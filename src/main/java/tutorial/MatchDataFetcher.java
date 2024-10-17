package tutorial;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.List;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;

public class MatchDataFetcher {
    
    public static List<Match> fetchMatches(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        String jsonString = response.body();
        JSONObject obj = new JSONObject(jsonString);
        JSONArray events = obj.getJSONArray("events");
        System.out.println("Se han encontrado un total de " + events.length() + " partidos.");

        List<Match> matches = new ArrayList<>();

        for (int i = 0; i < events.length(); i++) {
            Match match = extractMatchData(events.getJSONObject(i));
            matches.add(match);
        }

        return matches;
    }

    private static Match extractMatchData(JSONObject event) throws IOException, InterruptedException {
        
        Match match = new Match();
        match.eventId = event.getInt("id");
        System.out.println("Inicio: ");
        System.out.println(match.eventId);
        
        match.country = event.getJSONObject("tournament").getJSONObject("category").optString("name");
        match.tournament = event.getJSONObject("tournament").optString("name");
        match.homeTeam = event.getJSONObject("homeTeam").optString("name");
        match.awayTeam = event.getJSONObject("awayTeam").optString("name");
        match.homeScore = event.getJSONObject("homeScore").optInt("current");
        match.awayScore = event.getJSONObject("awayScore").optInt("current");
        match.status = event.getJSONObject("status").optInt("code");
        long currentPeriodStartTimestamp = event.getJSONObject("time").optLong("currentPeriodStartTimestamp");
        long unixTimestamp = Instant.now().getEpochSecond();
        long diffTimestamp = unixTimestamp - currentPeriodStartTimestamp;
        match.minutes = (match.status == 7 ? (diffTimestamp / 60) + 45
                : match.status == 41 ? (diffTimestamp / 60) + 90
                : match.status == 42 ? (diffTimestamp / 60) + 105
                : match.status == 31 ? 45 : diffTimestamp / 60);
        match.hasStats = event.getJSONObject("tournament").has("uniqueTournament")
                && event.getJSONObject("tournament").getJSONObject("uniqueTournament")
                        .optBoolean("hasEventPlayerStatistics", false);
        if (match.hasStats) {
            String statsUrl = "https://api.sofascore.com/api/v1/event/" + match.eventId + "/statistics";
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
                    if (stats.getJSONObject(j).optString("groupName").equals("Shots")) {
                        JSONArray shots = stats.getJSONObject(j).getJSONArray("statisticsItems");
                        for (int k = 0; k < shots.length(); k++) {
                            if (shots.getJSONObject(k).optString("name").equals("Total shots")) {
                                match.homeShots = shots.getJSONObject(k).optString("home");
                                match.awayShots = shots.getJSONObject(k).optString("away");
                            } else if (shots.getJSONObject(k).optString("name").equals("Shots on target")) {
                                match.homeShotsOn = shots.getJSONObject(k).optString("home");
                                match.awayShotsOn = shots.getJSONObject(k).optString("away");
                            } else if (shots.getJSONObject(k).optString("name").equals("Shots off target")) {
                                match.homeShotsOff = shots.getJSONObject(k).optString("home");
                                match.awayShotsOff = shots.getJSONObject(k).optString("away");
                            } else if (shots.getJSONObject(k).optString("name").equals("Blocked shots")) {
                                match.homeShotsBlocked = shots.getJSONObject(k).optString("home");
                                match.awayShotsBlocked = shots.getJSONObject(k).optString("away");
                            }
                        }
                    } else if (stats.getJSONObject(j).optString("groupName").equals("TVData")) {
                        JSONArray data = stats.getJSONObject(j).getJSONArray("statisticsItems");
                        for (int k = 0; k < data.length(); k++) {
                            if (data.getJSONObject(k).optString("name").equals("Corner kicks")) {
                                match.homeCorners = data.getJSONObject(k).optString("home", "0");
                                match.awayCorners = data.getJSONObject(k).optString("away", "0");
                            }
                        }
                    } else if (stats.getJSONObject(j).optString("groupName").equals("Possession")) {
                        JSONArray data = stats.getJSONObject(j).getJSONArray("statisticsItems");
                        for (int k = 0; k < data.length(); k++) {
                            if (data.getJSONObject(k).optString("name").equals("Ball possession")) {
                                match.homePossession = data.getJSONObject(k).optString("home");
                                match.awayPossession = data.getJSONObject(k).optString("away");
                            }
                        }
                    }
                }

                //Start getting odds
                String oddsUrl = "https://api.sofascore.com/api/v1/event/" + match.eventId + "/odds/1/all";
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

                // System.out.println("Status: " + match.status + " - " + match.toString());
                System.out.println("Fin");

                
            }
        }
        return match;
    }

    public static List<Match> fetchTennisMatches(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        String jsonString = response.body();
        JSONObject obj = new JSONObject(jsonString);
        JSONArray events = obj.getJSONArray("events");
        System.out.println("Se han encontrado un total de " + events.length() + " partidos.");

        List<Match> matches = new ArrayList<>();

        for (int i = 0; i < events.length(); i++) {
            Match match = extractTennisMatchData(events.getJSONObject(i));
            matches.add(match);
        }

        return matches;
    }

    private static Match extractTennisMatchData(JSONObject event) throws IOException, InterruptedException {
        
        Match match = new Match();
        match.eventId = event.getInt("id");
        
        if(event.getJSONObject("homeTeam").optString("gender").equals("F") && event.getJSONObject("awayTeam").optString("gender").equals("F")) {
            match.tournament = event.getJSONObject("tournament").optString("name");
            match.homeTeam = event.getJSONObject("homeTeam").optString("name");
            match.awayTeam = event.getJSONObject("awayTeam").optString("name");
            match.homeScore = event.getJSONObject("homeScore").optInt("period1");
            match.awayScore = event.getJSONObject("awayScore").optInt("period1");

            //Start getting odds
            String oddsUrl = "https://api.sofascore.com/api/v1/event/" + match.eventId + "/odds/1/all";
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
            if(oddsObj.has("markets")) {
                JSONArray markets = oddsObj.getJSONArray("markets");
                if(markets.length() > 1 && markets.getJSONObject(1).optString("marketName").equals("Full time")) {
                    JSONObject homeOddsData = markets.getJSONObject(1).getJSONArray("choices").getJSONObject(0);
                    JSONObject awayOddsData = markets.getJSONObject(1).getJSONArray("choices").getJSONObject(1);
                    String homeFraction = homeOddsData.optString("fractionalValue");
                    String preMatchHomeFraction = homeOddsData.optString("initialFractionalValue");
                    String awayFraction = awayOddsData.optString("fractionalValue");
                    String preMatchAwayFraction = awayOddsData.optString("initialFractionalValue");
                    
                    //Convert fractions to EU odds
                    match.homeOdds = OddsConverter.convertFractionStringOddsToIntegerOdds(homeFraction);
                    match.preMatchHomeOdds = OddsConverter.convertFractionStringOddsToIntegerOdds(preMatchHomeFraction);
                    match.awayOdds = OddsConverter.convertFractionStringOddsToIntegerOdds(awayFraction);
                    match.preMatchAwayOdds = OddsConverter.convertFractionStringOddsToIntegerOdds(preMatchAwayFraction);
                }
            }
        }

        return match;
    }
}
