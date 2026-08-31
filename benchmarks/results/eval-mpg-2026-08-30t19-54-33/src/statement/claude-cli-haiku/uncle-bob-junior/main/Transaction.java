import java.time.*;

class Transaction {
    private final LocalDate date;
    private final String description;
    private final double amountEur;
    private final TransactionCategory category;
    private final boolean suspicious;

    Transaction(LocalDate date, String description, double amountEur,
                TransactionCategory category, boolean suspicious) {
        this.date = date;
        this.description = description;
        this.amountEur = amountEur;
        this.category = category;
        this.suspicious = suspicious;
    }

    LocalDate date() { return date; }
    String description() { return description; }
    double amountEur() { return amountEur; }
    TransactionCategory category() { return category; }
    boolean isSuspicious() { return suspicious; }
    YearMonth month() { return YearMonth.from(date); }
}
