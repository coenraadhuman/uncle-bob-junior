import java.math.BigDecimal;
import java.time.LocalDate;

class Transaction {
    private final LocalDate date;
    private final String description;
    private final BigDecimal amountEur;
    private final Category category;
    
    Transaction(LocalDate date, String description, BigDecimal amountEur, Category category) {
        this.date = date;
        this.description = description;
        this.amountEur = amountEur;
        this.category = category;
    }
    
    LocalDate date() { return date; }
    String description() { return description; }
    BigDecimal amountEur() { return amountEur; }
    Category category() { return category; }
}
