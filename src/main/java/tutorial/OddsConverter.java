package tutorial;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class OddsConverter {

    private static final DecimalFormat df = new DecimalFormat("#.##");

    static {
        df.setRoundingMode(RoundingMode.HALF_UP);
    }

    public static double convertFractionStringOddsToIntegerOdds(String fraction) {
        String[] parts = fraction.split("/");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid fraction format: " + fraction);
        }
        try {
            int numerator = Integer.parseInt(parts[0]);
            int denominator = Integer.parseInt(parts[1]);
            double odds = (double) numerator / denominator;
            return Double.parseDouble(df.format(odds + 1));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid fraction format: " + fraction, e);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Invalid fraction format: " + fraction, e);
        }
    }
}
