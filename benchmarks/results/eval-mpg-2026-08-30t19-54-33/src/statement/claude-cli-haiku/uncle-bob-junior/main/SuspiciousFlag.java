import java.math.BigDecimal;
import java.time.LocalDate;

class SuspiciousFlag {
    private final LocalDate date;
    private final String description;
    private final BigDecimal amount;
    private final String reason;

    SuspiciousFlag(LocalDate date, String description, BigDecimal amount, String reason) {
        this.date = date;
        this.description = description;
        this.amount = amount;
        this.reason = reason;
    }

    @Override
    public String toString() {
        return String.format("%s - %s - %.2f EUR: %s", date, description, amount, reason);
    }
}
