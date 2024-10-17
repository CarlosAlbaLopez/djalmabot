package tutorial;

public class TennisMatch {
    private String event;
    private String tournament;
    private String marketLine;
    private String matchUrl;

    public static final String emojiTennis = EmojiConstants.EMOJI_TENNIS;
    public static final String emojiTrophy = EmojiConstants.EMOJI_TROPHY;
    public static final String emojiChart = EmojiConstants.EMOJI_CHART_INCREASING;

    // Constructor
    public TennisMatch(String event, String tournament, String marketLine, String matchUrl) {
        this.event = event;
        this.tournament = tournament;
        this.marketLine = marketLine;
        this.matchUrl = matchUrl;
    }

    // Getters
    public String getEvent() {
        return event;
    }

    public String getTournament() {
        return tournament;
    }

    public String getMarketLine() {
        return marketLine;
    }

    public String getMatchUrl() {
        return matchUrl;
    }

    // toString method
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("<strong>Alerta Tenis</strong>\n\n" +
            emojiTennis + " " + event + "\n" +
            emojiTrophy + " " + tournament + "\n" +
            emojiChart + " " + marketLine + "\n\n" +
            matchUrl);
                    
        return (sb.toString());
    }
}
