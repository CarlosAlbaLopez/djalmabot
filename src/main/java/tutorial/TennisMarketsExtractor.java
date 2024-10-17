package tutorial;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import java.util.ArrayList;
import java.util.List;

public class TennisMarketsExtractor {
    public static List<TennisMatch> extractTennisMarkets(String htmlContent) {
        List<TennisMatch> Matches = new ArrayList<>();

        try {
            // Parse the HTML content
            Document doc = Jsoup.parse(htmlContent);

            // Declare variables
            String event, tournament, marketLine, matchUrl = "";

            // Extract matches ID
            Elements matchCardElements = doc.select("div[data-testid^=match-card-]");
            for (Element matchCard : matchCardElements) {
                // Clean variables
                event = "";
                tournament = "";
                marketLine = "";
                matchUrl = "";

                // Check current set
                Element currentSetDiv = matchCard.selectFirst("div.card-header-timer > div");
                if (currentSetDiv != null) {
                    String currentSet = currentSetDiv.html().trim();
                    if (!currentSet.isEmpty() && currentSet.contains("1er set")) {

                        
                        String matchId = matchCard.attr("data-testid").replace("match-card-", "");
                        System.out.println("Match: " + matchId);
                        
                        // Extract tournament name
                        Element tournamentElement = matchCard.selectFirst("div.match-subtitle-label");
                        if (tournamentElement != null) {
                            tournament = tournamentElement.text();
                        }
                        
                        if(!tournament.contains("Dobles")) {
                            System.out.println("Tournament: " + tournament);
                            // Extract player's names
                            Element playersCard = matchCard.selectFirst("div.full-match-title");
                            if(playersCard != null) {
                                Elements names = playersCard.select("div:matchesOwn(^[A-Z][a-z]+\\s[A-Z][a-z]+(\\s[A-Z][a-z]+)?$)");
                                for(Element name : names) {
                                    String player = name.text();
                                    if(event.isEmpty()) {
                                        event = event.concat(player + " - ");
                                    } else {
                                        event = event.concat(player);
                                    }
                                }
                            }
                    
                            if(event != null) {                        
                                System.out.println("Event: " + event);

                                // Extract match data
                                String matchContent;
                                matchUrl = "https://www.winamax.es/apuestas-deportivas/match/" + matchId;
                                
                                try (Playwright playwright = Playwright.create()) {
                                    Browser browser = playwright.chromium().launch();
                                    Page page = browser.newPage();
                                    page.navigate(matchUrl);
                                    page.evaluate("window.scrollTo(0, document.body.scrollHeight)");
                                    page.waitForTimeout(2000);
                                    matchContent = page.content();
                                    browser.close();
                                }
                                
                                // Navigate to match page
                                Document matchDoc = Jsoup.parse(matchContent);
                                Element matchData = matchDoc.selectFirst("div.match");
                                
                                if(matchData != null) {
                                    Element marketDiv14 = matchData.selectFirst("div:containsOwn(y menos de 14,5)");
                                    Element marketDiv15 = matchData.selectFirst("div:containsOwn(y menos de 15,5)");
                                    Element marketDiv16 = matchData.selectFirst("div:containsOwn(y menos de 16,5)");
                                    
                                    if(marketDiv14 != null) {
                                        marketLine = marketDiv14.html();
                                    } else if(marketDiv15 != null) {
                                        marketLine = marketDiv15.html();
                                    } else if(marketDiv16 != null) {
                                        marketLine = marketDiv16.html();
                                    }
                                }
                                
                                System.out.println("Market: " + marketLine);
                                
                                if(marketLine != null && !marketLine.isEmpty() && event != null && !event.isEmpty()) {
                                    // Create a new object and add it to the list
                                    TennisMatch tennisMatchRecord = new TennisMatch (event, tournament, marketLine, matchUrl);
                                    Matches.add(tennisMatchRecord);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Matches;
    }
}
