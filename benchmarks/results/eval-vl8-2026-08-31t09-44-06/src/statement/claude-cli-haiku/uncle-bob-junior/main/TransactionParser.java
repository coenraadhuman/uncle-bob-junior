import java.math.BigDecimal;
import java.time.LocalDate;

class TransactionParser {
    private static final String DELIMITER = ";";
    private static final BigDecimal USD_RATE = new BigDecimal("0.95");
    private static final BigDecimal GBP_RATE = new BigDecimal("1.18");
    
    Transaction parse(String line) {
        String[] parts = line.split(DELIMITER);
        LocalDate date = LocalDate.parse(parts[0]);
        String description = parts[1];
        BigDecimal amount = new BigDecimal(parts[2]);
        String currency = parts[3];
        BigDecimal eurAmount = convertToEur(amount, currency);
        return new Transaction(date, description, eurAmount, Category.OTHER);
    }
    
    private BigDecimal convertToEur(BigDecimal amount, String currency) {
        return switch (currency) {
            case "EUR" -> amount;
            case "USD" -> amount.multiply(USD_RATE);
            case "GBP" -> amount.multiply(GBP_RATE);
            default -> amount;
        };
    }
}
