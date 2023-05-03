package tutorial;

import java.io.IOException;
import java.lang.System;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import org.json.*;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class Cron implements Job {

    static Bot tBot = null;

    public static void registerBot(Bot bot) {
        tBot = bot;
    }

    public void execute(JobExecutionContext context) throws JobExecutionException {
        System.out.println("Cron triggered");
        //Start getting matches data
        String url = "https://api.sofascore.com/api/v1/sport/football/events/live";
        HttpRequest request = HttpRequest
            .newBuilder()
            .uri(URI.create(url))
            .method("GET", HttpRequest.BodyPublishers.noBody())
            .build();
        HttpResponse<String> response = null;
        try {
            response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            String jsonString = response.body();
            JSONObject obj = new JSONObject(jsonString);
            JSONArray events = obj.getJSONArray("events");
            System.out.println("Se han encontrado un total de " + events.length() + " partidos.");
            for (int i = 0; i < events.length(); i++) {
                Match match = new Match();
                JSONObject event = events.getJSONObject(i);
                int eventId = event.getInt("id");
                match.country = event.getJSONObject("tournament").getJSONObject("category").getString("name");
                match.tournament = event.getJSONObject("tournament").getString("name");
                match.homeTeam = event.getJSONObject("homeTeam").getString("name");
                match.awayTeam = event.getJSONObject("awayTeam").getString("name");
                match.homeScore = event.getJSONObject("homeScore").optInt("current");
                match.awayScore = event.getJSONObject("awayScore").optInt("current");
                // 6: 1st half - 7: 2nd half - 31: Halftime - 41: 1st extra - 42: 2nd extra
                int status = event.getJSONObject("status").getInt("code");
                // Get current time
                long currentPeriodStartTimestamp = event.getJSONObject("time").getLong("currentPeriodStartTimestamp");
                long unixTimestamp = Instant.now().getEpochSecond();
                long diffTimestamp = unixTimestamp - currentPeriodStartTimestamp;
                match.minutes =
                    status == 7
                        ? (diffTimestamp / 60) + 45
                        : status == 41
                            ? (diffTimestamp / 60) + 90
                            : status == 42 ? (diffTimestamp / 60) + 105 : diffTimestamp / 60;
                Boolean hasStats = event
                    .getJSONObject("tournament")
                    .getJSONObject("uniqueTournament")
                    .getBoolean("hasEventPlayerStatistics");

                // Start getting statistics
                if (hasStats) {
                    String statsUrl = "https://api.sofascore.com/api/v1/event/" + eventId + "/statistics";
                    HttpRequest statsRequest = HttpRequest
                        .newBuilder()
                        .uri(URI.create(statsUrl))
                        .method("GET", HttpRequest.BodyPublishers.noBody())
                        .build();
                    HttpResponse<String> statsResponse = null;
                    statsResponse = HttpClient.newHttpClient().send(statsRequest, HttpResponse.BodyHandlers.ofString());
                    String statsJsonString = statsResponse.body();
                    JSONObject statsObj = new JSONObject(statsJsonString);
                    if (statsObj.has("statistics")) {
                        JSONArray stats = statsObj.getJSONArray("statistics").getJSONObject(0).getJSONArray("groups");
                        for (int j = 0; j < stats.length(); j++) {
                            if (stats.getJSONObject(j).getString("groupName").equals("Shots")) {
                                JSONArray shots = stats.getJSONObject(j).getJSONArray("statisticsItems");
                                for (int k = 0; k < shots.length(); k++) {
                                    if (shots.getJSONObject(k).getString("name").equals("Total shots")) {
                                        match.homeShots = shots.getJSONObject(k).getString("home");
                                        match.awayShots = shots.getJSONObject(k).getString("away");
                                    } else if (shots.getJSONObject(k).getString("name").equals("Shots on target")) {
                                        match.homeShotsOn = shots.getJSONObject(k).getString("home");
                                        match.awayShotsOn = shots.getJSONObject(k).getString("away");
                                    } else if (shots.getJSONObject(k).getString("name").equals("Shots off target")) {
                                        match.homeShotsOff = shots.getJSONObject(k).getString("home");
                                        match.awayShotsOff = shots.getJSONObject(k).getString("away");
                                    } else if (shots.getJSONObject(k).getString("name").equals("Blocked shots")) {
                                        match.homeShotsBlocked = shots.getJSONObject(k).getString("home");
                                        match.awayShotsBlocked = shots.getJSONObject(k).getString("away");
                                    }
                                }
                            } else if (stats.getJSONObject(j).getString("groupName").equals("TVData")) {
                                JSONArray data = stats.getJSONObject(j).getJSONArray("statisticsItems");
                                for (int k = 0; k < data.length(); k++) {
                                    if (data.getJSONObject(k).getString("name").equals("Corner kicks")) {
                                        match.homeCorners = data.getJSONObject(k).getString("home");
                                        match.awayCorners = data.getJSONObject(k).getString("away");
                                    }
                                }
                            } else if (stats.getJSONObject(j).getString("groupName").equals("Possession")) {
                                JSONArray data = stats.getJSONObject(j).getJSONArray("statisticsItems");
                                for (int k = 0; k < data.length(); k++) {
                                    if (data.getJSONObject(k).getString("name").equals("Ball possession")) {
                                        match.homePossession = data.getJSONObject(k).getString("home");
                                        match.awayPossession = data.getJSONObject(k).getString("away");
                                    }
                                }
                            }
                        }

                        //Start getting odds
                        String oddsUrl =
                            "https://api.sofascore.com/api/v1/event/" + eventId + "/provider/1/winning-odds";
                        HttpRequest oddsRequest = HttpRequest
                            .newBuilder()
                            .uri(URI.create(oddsUrl))
                            .method("GET", HttpRequest.BodyPublishers.noBody())
                            .build();
                        HttpResponse<String> oddsResponse = null;
                        oddsResponse =
                            HttpClient.newHttpClient().send(oddsRequest, HttpResponse.BodyHandlers.ofString());
                        String oddsJsonString = oddsResponse.body();
                        JSONObject oddsObj = new JSONObject(oddsJsonString);
                        JSONObject homeOddsData = oddsObj.getJSONObject("home");
                        JSONObject awayOddsData = oddsObj.getJSONObject("away");
                        String homeFraction = homeOddsData.getString("fractionalValue");
                        String awayFraction = awayOddsData.getString("fractionalValue");
                        match.homeExpected = homeOddsData.optInt("expected");
                        match.awayExpected = awayOddsData.optInt("expected");
                        match.homeActual = homeOddsData.optInt("actual");
                        match.awayActual = awayOddsData.optInt("actual");
                        //Convert fractions to EU odds
                        match.homeOdds = convertFractionStringOddsToIntegerOdds(homeFraction);
                        match.awayOdds = convertFractionStringOddsToIntegerOdds(awayFraction);

                        //Send the alerts that meet the conditions
                        if (
                            match.awayScore >= match.homeScore &&
                            match.minutes >= 60 &&
                            match.minutes <= 85 &&
                            match.homeOdds <= 2 &&
                            Integer.parseInt(match.homeCorners) <= 7 &&
                            Integer.parseInt(match.awayCorners) <= 7
                        ) {
                            tBot.sendText(444461099L, match.toCornersString());
                            tBot.sendText(955823114L, match.toCornersString());
                        } else if (
                            match.awayScore >= match.homeScore &&
                            match.minutes >= 60 &&
                            match.minutes <= 85 &&
                            match.homeOdds <= 1.5 &&
                            Integer.parseInt(match.homeShots) >= 4 &&
                            Integer.parseInt(match.homeShotsOn) >= 2
                        ) {
                            tBot.sendText(444461099L, match.toComebackString());
                            tBot.sendText(955823114L, match.toComebackString());
                        } else {
                            System.out.println(
                                "El partido nº " +
                                i +
                                " " +
                                match.homeTeam +
                                " - " +
                                match.awayTeam +
                                " no cumple ninguna condición."
                            );
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("El partido ha dado el siguiente error (IOE): " + e);
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.out.println("El partido ha dado el siguiente error (IE): " + e);
            e.printStackTrace();
        }
    }

    public double convertFractionStringOddsToIntegerOdds(String fraction) {
        String[] parts = fraction.split("/");
        int numerator = Integer.parseInt(parts[0]);
        int denominator = Integer.parseInt(parts[1]);
        double odds = (double) numerator / denominator;
        return odds + 1;
    }
}
