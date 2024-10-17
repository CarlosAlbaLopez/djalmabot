package tutorial;

import java.lang.System;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.github.cdimascio.dotenv.Dotenv;

public class ValueCron implements Job {
    Dotenv dotenv = Dotenv.load(); // Load the .env file
    String apiKey = dotenv.get("SUREAPIKEY"); // Get the API key

    Gson gson = new GsonBuilder().create();

    static Bot tBot = null;
    static List<Integer> sentIds = new ArrayList<Integer>();
    static List<Long> sentValuebets = new ArrayList<Long>();

    public static void registerBot(Bot bot) {
        tBot = bot;
    }

    public void execute(JobExecutionContext context) throws JobExecutionException {
        System.out.println(
            "Value Cron triggered " + LocalDateTime.now().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT))
        );

        //Start getting matches data
        try {
            // Base URL and parameters
            String baseUrl = "https://api.apostasseguras.com/request";
            String product = "valuebets";
            String source = "bet365";
            // String sports = "Badminton|Baseball|Basketball|Basketball_3x3|CounterStrike|Darts|Dota|E_Basketball|E_Football|Football|Futsal|Handball|LeagueOfLegends|MobileLegends|Overwatch|Rugby|TableTennis|Tennis|Valorant|Volleyball";
            String sports = "CounterStrike|Dota|E_Basketball|E_Football|LeagueOfLegends|MobileLegends|Overwatch|TableTennis|Tennis|Valorant";
            String limit = "200";
            String oddsFormat = "eu";
            String minGroupSize = "2";

            // Encoding the necessary parameters
            String encodedSource = URLEncoder.encode(source, StandardCharsets.UTF_8);
            String encodedSports = URLEncoder.encode(sports, StandardCharsets.UTF_8);
            
            // Construct the full URL
            String url = baseUrl + "?product=" + product + "&source=" + encodedSource + "&sport=" + encodedSports + "&limit=" + limit + "&oddsFormat=" + oddsFormat + "&min_group_size=" + minGroupSize;

            // Create an HTTP client
            HttpClient client = HttpClient.newHttpClient();
            // Create the HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + apiKey)
                .GET()
                .build();

            // Send the request and get the response
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Check the response status code
            if (response.statusCode() == 200) {
                // Print the response body if the request was successful
                // System.out.println("Response: " + response.body());
            } else {
                // Print the status code and response body if the request failed
                System.out.println("Request failed. Response code: " + response.statusCode());
                System.out.println("Response Body: " + response.body());
            }

            List<JSONObject> filteredRecords = FilterResponse.filterRecords(response.body());

            // Print the filtered records
            for (JSONObject record : filteredRecords) {
                // Deserializar el JSON en un objeto ValueBet
                String jsonString = record.toString();
                ValueBet valueBet = gson.fromJson(jsonString, ValueBet.class);
                
                if (
                    !sentValuebets.contains(valueBet.getEvent_id())
                ) {
                    // Send alert
                    tBot.sendText(444461099L, valueBet.toString()); //yo
                    // tBot.sendText(-1001241643895L, valueBet.toString()); //channel
                    // Add event id to List
                    sentValuebets.add(valueBet.getEvent_id());
                    System.out.println("Enviada alerta en el partido: " + valueBet.getEvent_id());
                }
            }

            System.out.println(sentValuebets);
            System.out.println("Todos los partidos revisados.");
        } catch (Exception e) {
            System.out.println("El partido ha dado el siguiente error (E): " + e);
            e.printStackTrace();
        }
    }
}
