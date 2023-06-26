package tutorial;

public class OddsConverter {

    public static Double convertFractionStringOddsToIntegerOdds(String fraction) {
        String[] parts = fraction.split("/");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid fraction format: " + fraction);
        }
        try {
            int numerator = Integer.parseInt(parts[0]);
            int denominator = Integer.parseInt(parts[1]);
            double odds = (double) numerator / denominator;
            return Double.parseDouble(String.format("%.2f", odds + 1).replaceAll(",", "."));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid fraction format: " + fraction, e);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Invalid fraction format: " + fraction, e);
        }
    }
}
