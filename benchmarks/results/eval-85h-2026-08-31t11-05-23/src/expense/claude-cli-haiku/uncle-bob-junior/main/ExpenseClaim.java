import java.time.YearMonth;

public record ExpenseClaim(
    String employeeId,
    String employeeName,
    YearMonth month,
    int amountEuros,
    ExpenseCategory category,
    boolean receiptAttached,
    String description
) {}
