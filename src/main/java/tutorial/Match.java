package tutorial;

public class Match {

    public long minutes;
    public String country;
    public String tournament;
    public String homeTeam;
    public String awayTeam;
    public int homeScore;
    public int awayScore;
    public String homeCorners;
    public String awayCorners;
    public String homeShots;
    public String awayShots;
    public String homeShotsOn;
    public String awayShotsOn;
    public String homeShotsOff;
    public String awayShotsOff;
    public String homeShotsBlocked;
    public String awayShotsBlocked;
    public String homePossession;
    public String awayPossession;
    public int homeExpected;
    public int awayExpected;
    public int homeActual;
    public int awayActual;
    public double homeOdds;
    public double awayOdds;
    //Icons
    String emojiBall = "\u26BD";
    String emojiCorner = "\u26F3";
    String emojiGun = "\u1F52B";
    String emojiRugby = "\u1F3C9";
    String emojiData = "\u1F4C8";
    String emojiChart = "\u1F4CA";
    String emojiExclamation = "\u2757";

    public Match() {}

    public String toCornersString() {
        return (
            "<strong>Alerta Carrera a Córners</strong>\n\n" +
            minutes +
            "' | <strong>" +
            country +
            "</strong> | " +
            tournament +
            "\n<strong>" +
            homeTeam +
            "</strong> (" +
            homeScore +
            " - " +
            awayScore +
            ") " +
            awayTeam +
            "\n\n<em>Datos live:</em>\n" +
            "Cuota prepartido 12: <strong>" +
            homeOdds +
            "</strong> | <strong>" +
            awayOdds +
            "</strong>\nProbabilidades según cuotas 12:" +
            homeExpected +
            " | " +
            awayExpected +
            "\nProbabilidades reales 12:" +
            homeActual +
            " | " +
            awayActual +
            "\n\n<em>Info de mercado:</em>\n\t" +
            emojiCorner +
            "Corners: <strong>" +
            homeCorners +
            "</strong>-" +
            awayCorners +
            "\n\t" +
            emojiBall +
            "Tiros: <strong>" +
            homeShots +
            "</strong>-" +
            awayShots +
            "\n\tTiros a puerta: <strong>" +
            homeShotsOn +
            "</strong>-" +
            awayShotsOn +
            "\n\tTiros fuera: <strong>" +
            homeShotsOff +
            "</strong>-" +
            awayShotsOff +
            "\n\tTiros bloqueados: <strong>" +
            homeShotsBlocked +
            "</strong>-" +
            awayShotsBlocked +
            "\n\tPosesión: <strong>" +
            homePossession +
            "</strong>-" +
            awayPossession
        );
    }

    public String toComebackString() {
        return (
            "<strong>Alerta Remontada</strong>\n\n" +
            minutes +
            "' | <strong>" +
            country +
            "</strong> | " +
            tournament +
            "\n<strong>" +
            homeTeam +
            "</strong> (" +
            homeScore +
            " - " +
            awayScore +
            ") " +
            awayTeam +
            "\n\n<em>Info de mercado:</em>\n" +
            "Cuota prepartido 12: <strong>" +
            homeOdds +
            "</strong> | <strong>" +
            awayOdds +
            "</strong>\nProbabilidades según cuotas 12:" +
            homeExpected +
            " | " +
            awayExpected +
            "\nProbabilidades reales 12:" +
            homeActual +
            " | " +
            awayActual +
            "\n\n<em>Datos live:</em>\n\t" +
            emojiBall +
            "Tiros: <strong>" +
            homeShots +
            "</strong>-" +
            awayShots +
            "\n\tTiros a puerta: <strong>" +
            homeShotsOn +
            "</strong>-" +
            awayShotsOn +
            "\n\tTiros fuera: <strong>" +
            homeShotsOff +
            "</strong>-" +
            awayShotsOff +
            "\n\tTiros bloqueados: <strong>" +
            homeShotsBlocked +
            "</strong>-" +
            awayShotsBlocked +
            "\n\tPosesión: <strong>" +
            homePossession +
            "</strong>-" +
            awayPossession +
            "\n\t" +
            emojiCorner +
            "Corners: <strong>" +
            homeCorners +
            "</strong>-" +
            awayCorners
        );
    }
}
