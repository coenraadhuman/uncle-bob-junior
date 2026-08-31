import java.math.BigDecimal;
import java.time.LocalDate;

class TransactionParser {
    static Transaction parse(String line) {
        try {
            String[] parts = line.split(";");
            if (parts.length != 4) return null;
            
            LocalDate date = LocalDate.parse(parts[0].trim());
            String description = parts[1].trim();
            BigDecimal amount = new BigDecimal(parts[2].trim());
            String currency = parts[3].trim();
            
            return new Transaction(date, description, amount, currency);
        } catch (Exception e) {
            return null;
        }
    }
}
