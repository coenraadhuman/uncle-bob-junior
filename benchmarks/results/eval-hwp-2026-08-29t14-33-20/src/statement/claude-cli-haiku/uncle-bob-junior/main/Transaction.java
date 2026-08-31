import java.time.*;
import java.util.*;

record Transaction(
    LocalDate date,
    String description,
    double amount,
    String currency,
    double amountEur,
    String category,
    boolean suspicious
) {
    static Transaction parse(String line) {
        var parts = line.split(";");
        if (parts.length != 4) throw new IllegalArgumentException("Invalid: " + line);
        var date = LocalDate.parse(parts[0]);
        var desc = parts[1].trim();
        var amt = Double.parseDouble(parts[2]);
        var curr = parts[3].trim();
        var eurAmt = convertToEur(amt, curr);
        var cat = categorizeTransaction(desc);
        return new Transaction(date, desc, amt, curr, eurAmt, cat, false);
    }
    
    private static double convertToEur(double amount, String currency) {
        return switch (currency) {
            case "USD" -> amount * 0.92;
            case "GBP" -> amount * 1.17;
            case "EUR" -> amount;
            default -> throw new IllegalArgumentException("Unknown: " + currency);
        };
    }
    
    private static String categorizeTransaction(String description) {
        var lower = description.toLowerCase();
        if (hasAny(lower, "salary", "wage", "payment", "deposit")) return "salary";
        if (hasAny(lower, "rent", "landlord", "lease")) return "rent";
        if (hasAny(lower, "albert heijn", "supermarket", "grocery", "food", "market")) return "groceries";
        return "other";
    }
    
    private static boolean hasAny(String text, String... keywords) {
        return Arrays.stream(keywords).anyMatch(text::contains);
    }
    
    Transaction withSuspicious() {
        return new Transaction(date, description, amount, currency, amountEur, category, true);
    }
}
