import java.math.BigDecimal;
import java.time.LocalDate;

class TransactionInEur {
    private final LocalDate date;
    private final String description;
    private final BigDecimal eurAmount;
    private final String category;

    TransactionInEur(LocalDate date, String description, BigDecimal eurAmount, String category) {
        this.date = date;
        this.description = description;
        this.eurAmount = eurAmount;
        this.category = category;
    }

    LocalDate getDate() { return date; }
    String getDescription() { return description; }
    BigDecimal getAmount() { return eurAmount; }
    String getCategory() { return category; }
}
