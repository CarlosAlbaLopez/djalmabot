package tutorial;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Date;

public class ValueBet {
    private String sport_id;
    private List<String> teams;
    private double probability;
    private String bk; // Bookmaker
    private long sortBy;
    private String tournament;
    private Type type;
    private long tournamentId;
    private int similarSize;
    private String eu; // Odds
    private long event_id;
    private double commission;
    private PreferredNav preferredNav;
    private long id;
    private long time;
    private double overvalue;
    private double value;

    public static final String emojiTennis = EmojiConstants.EMOJI_TENNIS;
    public static final String emojiBasketball = EmojiConstants.EMOJI_BASKETBALL;
    public static final String emojiSoccer = EmojiConstants.EMOJI_SOCCER;
    public static final String emojiDarts = EmojiConstants.EMOJI_DARTS;
    public static final String emojiTableTennis = EmojiConstants.EMOJI_TABLE_TENNIS;
    public static final String emojiEsports = EmojiConstants.EMOJI_VIDEO_GAME;
    public static final String emojiTrophy = EmojiConstants.EMOJI_TROPHY;
    public static final String emojiChart = EmojiConstants.EMOJI_CHART_INCREASING;
    public static final String emojiMoney = EmojiConstants.EMOJI_MONEY_BAG;
    public static final String emojiDice = EmojiConstants.EMOJI_DICE;
    public static final String emojiStake = EmojiConstants.EMOJI_BANK;
    public static final String emojiClock = EmojiConstants.EMOJI_CLOCK;

    // Constructor
    public ValueBet(String sport_id, List<String> teams, double probability, String bk, long sortBy,
                    String tournament, Type type, long tournamentId, int similarSize, String eu,
                    long event_id, double commission, PreferredNav preferredNav, long id, long time,
                    double overvalue, double value) {
        this.sport_id = sport_id;
        this.teams = teams;
        this.probability = probability;
        this.bk = bk;
        this.sortBy = sortBy;
        this.tournament = tournament;
        this.type = type;
        this.tournamentId = tournamentId;
        this.similarSize = similarSize;
        this.eu = eu;
        this.event_id = event_id;
        this.commission = commission;
        this.preferredNav = preferredNav;
        this.id = id;
        this.time = time;
        this.overvalue = overvalue;
        this.value = value;
    }

    // Getters and Setters
    // (Omitted for brevity; you can generate them using an IDE or manually)
    public long getEvent_id() {
        return event_id;
    }

    // Nested classes
    public static class Type {
        private String condition;
        private String game;
        private String period;
        private String variety;
        private String type;
        private String base;

        // Constructor
        public Type(String game, String period, String variety, String type, String base) {
            this.game = game;
            this.period = period;
            this.variety = variety;
            this.type = type;
            this.base = base;
        }

        public Type(String condition, String game, String period, String variety, String type, String base) {
            this.condition = condition;
            this.game = game;
            this.period = period;
            this.variety = variety;
            this.type = type;
            this.base = base;
        }

        // Getters and Setters
        // (Omitted for brevity)

        @Override
        public String toString() {
            if(condition != null && !condition.isEmpty() && (base.equals("home") || base.equals("away"))) {
                return base + " " + type + " " + condition + " " + variety + " ";
            } else if (condition != null && !condition.isEmpty()) {
                return type + " " + condition + " " + variety + " ";
            } else if (base.equals("home") || base.equals("away")) {
                return base + " " + type + " " + variety + " ";
            } else {
                return type + " " + variety + " ";
            }
        }
    }

    public static class PreferredNav {
        private boolean direct;
        private List<Link> links;

        // Constructor
        public PreferredNav(boolean direct, List<Link> links) {
            this.direct = direct;
            this.links = links;
        }

        // Getters and Setters
        // (Omitted for brevity)

        public static class Link {
            private String name;
            private LinkDetails link;

            // Constructor
            public Link(String name, LinkDetails link) {
                this.name = name;
                this.link = link;
            }

            // Getters and Setters
            // (Omitted for brevity)

            public static class LinkDetails {
                private String method;
                private LinkParams params;
                private String url;

                // Constructor
                public LinkDetails(String method, LinkParams params, String url) {
                    this.method = method;
                    this.params = params;
                    this.url = url;
                }

                // Getters and Setters
                // (Omitted for brevity)

                public static class LinkParams {
                    private String nr;

                    // Constructor
                    public LinkParams(String nr) {
                        this.nr = nr;
                    }

                    // Getters and Setters
                    // (Omitted for brevity)
                }
            }
        }
    }

    @Override
    public String toString() {
        // Format data
        String formattedTeams = String.join(" - ", teams);

        BigDecimal bdProbability = new BigDecimal(probability * 100).setScale(2, RoundingMode.HALF_UP);
        double roundedProbability = bdProbability.doubleValue();

        BigDecimal bdOvervalue = new BigDecimal((overvalue - 1) * 100).setScale(2, RoundingMode.HALF_UP);
        double roundedOvervalue = bdOvervalue.doubleValue();

        // Calculate Kelly
        double odds = Double.parseDouble(eu);
        double kellyValue = ((odds - 1) * probability - (1 - probability)) / (odds - 1);
        int stake = 1;
        if(kellyValue > 0.2) {
            stake = 4;
        } else if(kellyValue > 0.15) {
            stake = 3;
        } else if(kellyValue > 0.125) {
            stake = 2;
        }

        // Construct output string
        StringBuilder sb = new StringBuilder();

        // sb.append("<strong>Value Bet365</strong>\n\n");

        if(sport_id.equals("Basketball")) {
            sb.append(emojiBasketball);
        } else if(sport_id.equals("Football")) {
            sb.append(emojiSoccer);
        }  else if(sport_id.equals("Tennis")) {
            sb.append(emojiTennis);
        }  else if(sport_id.equals("Darts")) {
            sb.append(emojiDarts);
        } else if(sport_id.equals("TableTennis")) {
            sb.append(emojiTableTennis);
        } else {
            sb.append(emojiEsports);
        }

        sb.append(" <strong>" + formattedTeams + "</strong>\n" +
            emojiTrophy + " " + tournament + "\n" +
            emojiClock + " " + new Date(time) + "\n\n" +
            emojiMoney + " " + eu + "\n" +
            emojiDice + " " + roundedProbability + "%" + "\n" +
            emojiChart + " " + roundedOvervalue + "%" + "\n\n<strong>" +
            type.toString() + "</strong>\n\n" +
            emojiStake + " " + stake);
                    
        return (sb.toString());
    }
}
