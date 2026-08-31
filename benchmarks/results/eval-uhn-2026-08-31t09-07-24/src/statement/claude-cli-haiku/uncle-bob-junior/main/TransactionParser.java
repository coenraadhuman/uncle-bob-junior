import java.time.*;

class TransactionParser {
    private static final String SALARY_PATTERN = "salary|wage|income|payment";
    private static final String RENT_PATTERN = "rent|landlord|lease";
    private static final String GROCERIES_PATTERN = "supermarket|albert|heijn|grocery|food|shop";
    
    Transaction parse(String line) {
        String[] parts = line.split(";");
        if (parts.length != 4) {
            throw new IllegalArgumentException("Invalid format: " + line);
        }
        
        LocalDate date = LocalDate.parse(parts[0]);
        String description = parts[1];
        double amount = Double.parseDouble(parts[2]);
        String currency = parts[3];
        
        double amountEur = CurrencyConverter.toEur(amount, currency);
        TransactionCategory category = categorize(description);
        
        return new Transaction(date, description, amountEur, category);
    }
    
    private TransactionCategory categorize(String description) {
        String lower = description.toLowerCase();
        
        if (lower.matches(".*(" + SALARY_PATTERN + ").*")) {
            return TransactionCategory.SALARY;
        }
        if (lower.matches(".*(" + RENT_PATTERN + ").*")) {
            return TransactionCategory.RENT;
        }
        if (lower.matches(".*(" + GROCERIES_PATTERN + ").*")) {
            return TransactionCategory.GROCERIES;
        }
        return TransactionCategory.OTHER;
    }
}
