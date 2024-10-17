package tutorial;

import java.lang.System;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class Cron implements Job {

    static Bot tBot = null;
    static List<Integer> sentIds = new ArrayList<Integer>();
    static List<String> sentValuebets = new ArrayList<String>();

    public static void registerBot(Bot bot) {
        tBot = bot;
    }

    public void execute(JobExecutionContext context) throws JobExecutionException {
        System.out.println(
            "Cron triggered " + LocalDateTime.now().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT))
        );

        //Start getting matches data
        try {
            String url = "https://www.winamax.es/apuestas-deportivas/sports/5/213";
            List<TennisMatch> valueBets = URLReader.getValueBets(url);

            for (TennisMatch valueBet : valueBets) {
                if (
                    !sentValuebets.contains(valueBet.getEvent())
                ) {
                    // Send alert
                    // tBot.sendText(444461099L, valueBet.toString()); //yo
                    tBot.sendText(-1001241643895L, valueBet.toString()); //channel
                    // Add event id to List
                    sentValuebets.add(valueBet.getEvent());
                    System.out.println("Enviada alerta en el partido: " + valueBet.getEvent());
                }
            }
            System.out.println(sentValuebets);
            System.out.println("Todos los partidos revisados.");
        } catch (Exception e) {
            System.out.println("El partido ha dado el siguiente error (E): " + e);
            e.printStackTrace();
        }
    }

    // public void execute(JobExecutionContext context) throws JobExecutionException {
    //     System.out.println(
    //         "Cron triggered " + LocalDateTime.now().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT))
    //     );

    //     //Start getting matches data
    //     try {
    //         //Specify sports to fetch
    //         //List<Match> matches = MatchDataFetcher.fetchMatches("https://api.sofascore.com/api/v1/sport/football/events/live");
    //         List<Match> matches = MatchDataFetcher.fetchTennisMatches("https://api.sofascore.com/api/v1/sport/tennis/events/live");

    //         for (Match match : matches) {
    //             //Start getting statistics
    //             //Send the alerts that meet the conditions
    //             if (
    //                 match.awayScore >= match.homeScore &&
    //                 match.minutes >= 55 &&
    //                 match.minutes <= 85 &&
    //                 match.homeOdds <= 2 &&
    //                 match.homeCorners != null && !match.homeCorners.isEmpty() &&
    //                 match.awayCorners != null && !match.awayCorners.isEmpty() &&
    //                 Integer.parseInt(match.homeCorners) > Integer.parseInt(match.awayCorners) &&
    //                 Integer.parseInt(match.homeCorners) > 2 &&
    //                 Integer.parseInt(match.homeCorners) < 8 &&
    //                 Integer.parseInt(match.awayCorners) < 8 &&
    //                 !sentIds.contains(match.eventId)
    //             ) {
    //                 // tBot.sendText(444461099L, match.toCornersString()); //yo
    //                 // tBot.sendText(955823114L, match.toCornersString()); //carlitos
    //                 tBot.sendText(-1001241643895L, match.toCornersString()); //channel
    //                 // Add event id to List
    //                 sentIds.add(match.eventId);
    //                 System.out.println("Enviada alerta corners en el partido: " + match.toString());
    //             } else if (
    //                 match.awayScore >= match.homeScore &&
    //                 match.minutes >= 55 &&
    //                 match.minutes <= 85 &&
    //                 match.homeOdds <= 2 &&
    //                 match.homeShotsOn != null && !match.homeShotsOn.isEmpty() &&
    //                 Integer.parseInt(match.homeShotsOn) >= 2 &&
    //                 !sentIds.contains(match.eventId)
    //             ) {
    //                 // tBot.sendText(444461099L, match.toComebackString()); //yo
    //                 // tBot.sendText(955823114L, match.toComebackString()); //carlitos
    //                 // tBot.sendText(-1001241643895L, match.toCornersString()); //channel
    //                 // Add event id to List
    //                 sentIds.add(match.eventId);
    //             } else if ( // TENNIS F
    //                 !sentIds.contains(match.eventId) &&
    //                 ((match.preMatchHomeOdds >= 7 && match.homeScore == 2 && match.awayScore == 0) ||
    //                 (match.preMatchHomeOdds >= 7 && match.homeScore == 3 && (match.awayScore == 0 || match.awayScore == 1)) ||
    //                 ((match.preMatchAwayOdds >= 7 && match.awayScore == 2 && match.homeScore == 0) ||
    //                 (match.preMatchAwayOdds >= 7 && match.awayScore == 3 && (match.homeScore == 0 || match.homeScore == 1))))
    //             ) { 
    //                 tBot.sendText(-1001241643895L, match.toTennisString()); //channel
    //                 System.out.println("---------------------------------");
    //                 System.out.println("Match: " + match.eventId + " added to feed");
    //                 System.out.println("---------------------------------");
    //                 System.out.println(match.toTennisString());
    //                 // Add event id to List
    //                 sentIds.add(match.eventId);
    //             }
    //         }
    //         System.out.println(sentIds);
    //         System.out.println("Todos los partidos revisados.");
    //     } catch (IOException e) {
    //         System.out.println("El partido ha dado el siguiente error (IOE): " + e);
    //         e.printStackTrace();
    //     } catch (InterruptedException e) {
    //         System.out.println("El partido ha dado el siguiente error (IE): " + e);
    //         e.printStackTrace();
    //     } catch (Exception e) {
    //         System.out.println("El partido ha dado el siguiente error (E): " + e);
    //         e.printStackTrace();
    //     }
    // }
}
