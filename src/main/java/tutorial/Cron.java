package tutorial;

import java.io.IOException;
import java.lang.System;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.json.*;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class Cron implements Job {

    static Bot tBot = null;
    static List<Integer> sentIds = new ArrayList<Integer>();

    public static void registerBot(Bot bot) {
        tBot = bot;
    }

    public void execute(JobExecutionContext context) throws JobExecutionException {
        System.out.println("Cron triggered " + java.time.LocalDateTime.now());
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
                //System.out.println(i);
                Match match = new Match();
                JSONObject event = events.getJSONObject(i);
                int eventId = event.getInt("id");
                //System.out.println(eventId);
                match.country = event.getJSONObject("tournament").getJSONObject("category").getString("name");
                match.tournament = event.getJSONObject("tournament").getString("name");
                match.homeTeam = event.getJSONObject("homeTeam").getString("name");
                match.awayTeam = event.getJSONObject("awayTeam").getString("name");
                match.homeScore = event.getJSONObject("homeScore").optInt("current");
                match.awayScore = event.getJSONObject("awayScore").optInt("current");
                // 6: 1st half - 7: 2nd half - 31: Halftime - 41: 1st extra - 42: 2nd extra
                int status = event.getJSONObject("status").optInt("code");
                // Get current time
                long currentPeriodStartTimestamp = event.getJSONObject("time").optLong("currentPeriodStartTimestamp");
                long unixTimestamp = Instant.now().getEpochSecond();
                long diffTimestamp = unixTimestamp - currentPeriodStartTimestamp;
                match.minutes =
                    status == 7
                        ? (diffTimestamp / 60) + 45
                        : status == 41
                            ? (diffTimestamp / 60) + 90
                            : status == 42 ? (diffTimestamp / 60) + 105 : diffTimestamp / 60;
                Boolean hasStats = false;
                if (event.getJSONObject("tournament").has("uniqueTournament")) {
                    hasStats =
                        event
                            .getJSONObject("tournament")
                            .getJSONObject("uniqueTournament")
                            .optBoolean("hasEventPlayerStatistics", false);
                }

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
                        String oddsUrl = "https://api.sofascore.com/api/v1/event/" + eventId + "/odds/1/all";
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
                        JSONArray markets = oddsObj.getJSONArray("markets");
                        JSONObject homeOddsData = markets.getJSONObject(0).getJSONArray("choices").getJSONObject(0);
                        JSONObject drawOddsData = markets.getJSONObject(0).getJSONArray("choices").getJSONObject(1);
                        JSONObject awayOddsData = markets.getJSONObject(0).getJSONArray("choices").getJSONObject(2);
                        String homeFraction = homeOddsData.optString("fractionalValue");
                        String drawFraction = drawOddsData.optString("fractionalValue");
                        String awayFraction = awayOddsData.optString("fractionalValue");
                        //Convert fractions to EU odds
                        match.homeOdds = OddsConverter.convertFractionStringOddsToIntegerOdds(homeFraction);
                        match.drawOdds = OddsConverter.convertFractionStringOddsToIntegerOdds(drawFraction);
                        match.awayOdds = OddsConverter.convertFractionStringOddsToIntegerOdds(awayFraction);
                        //Corners
                        for (int j = 0; j < markets.length(); j++) {
                            if (markets.getJSONObject(j).optInt("marketId") == 21) {
                                match.cornerLine = markets.getJSONObject(j).optString("choiceGroup");
                                break;
                            }
                        }

                        //Send the alerts that meet the conditions
                        if (
                            match.awayScore >= match.homeScore &&
                            match.minutes >= 60 &&
                            match.minutes <= 85 &&
                            match.homeOdds <= 2 &&
                            Integer.parseInt(match.homeCorners) <= 7 &&
                            Integer.parseInt(match.awayCorners) <= 7 &&
                            !sentIds.contains(eventId)
                        ) {
                            tBot.sendText(444461099L, match.toCornersString());
                            tBot.sendText(955823114L, match.toCornersString());
                            sentIds.add(eventId);
                            System.out.println("Enviada alerta corners el el partido: " + match.toString());
                        } else if (
                            match.awayScore >= match.homeScore &&
                            match.minutes >= 60 &&
                            match.minutes <= 85 &&
                            match.homeOdds <= 1.5 &&
                            Integer.parseInt(match.homeShots) >= 4 &&
                            Integer.parseInt(match.homeShotsOn) >= 2 &&
                            !sentIds.contains(eventId)
                        ) {
                            tBot.sendText(444461099L, match.toComebackString());
                            tBot.sendText(955823114L, match.toComebackString());
                            sentIds.add(eventId);
                            System.out.println("Enviada alerta remontada el el partido: " + match.toString());
                        }
                    }
                }
            }
            System.out.println("Todos los partidos revisados.");
        } catch (IOException e) {
            System.out.println("El partido ha dado el siguiente error (IOE): " + e);
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.out.println("El partido ha dado el siguiente error (IE): " + e);
            e.printStackTrace();
        }
    }
}
