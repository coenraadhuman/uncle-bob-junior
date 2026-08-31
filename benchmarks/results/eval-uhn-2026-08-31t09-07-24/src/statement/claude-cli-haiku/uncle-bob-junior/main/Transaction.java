import java.time.*;

class Transaction {
    final LocalDate date;
    final String description;
    final double amountEur;
    final TransactionCategory category;
    
    Transaction(LocalDate date, String description, double amountEur, TransactionCategory category) {
        this.date = date;
        this.description = description;
        this.amountEur = amountEur;
        this.category = category;
    }
}
