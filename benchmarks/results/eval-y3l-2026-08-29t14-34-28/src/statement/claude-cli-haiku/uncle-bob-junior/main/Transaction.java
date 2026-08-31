import java.time.*;

record Transaction(
    LocalDate date,
    String description,
    double amount,
    String currency,
    double amountEur,
    String category
) {}
