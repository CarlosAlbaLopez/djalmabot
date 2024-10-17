package tutorial;

import java.util.List;

import com.microsoft.playwright.*;

public class URLReader {

    // Method to fetch HTML content and parse ValueBets
    public static List<TennisMatch> getValueBets(String urlString) {
        // StringBuilder content = new StringBuilder();
        String content;

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch();
            Page page = browser.newPage();
            page.navigate(urlString);
            content = page.content();
            // System.out.println(content);
            browser.close();
        }

        // Use ValueBetExtractor to parse and extract information
        //return ValueBetExtractor.extractValueBets(content);
        return TennisMarketsExtractor.extractTennisMarkets(content);
    }
}
