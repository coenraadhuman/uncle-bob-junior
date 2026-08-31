import java.time.YearMonth;

class ExpenseClaim {
    final String claimId;
    final String employeeId;
    final String employeeName;
    final YearMonth month;
    final Category category;
    final int amountEuro;
    final boolean receiptAttached;
    final String description;
    ApprovalStatus status;
    String validationError;

    ExpenseClaim(String claimId, String employeeId, String employeeName, YearMonth month,
                 Category category, int amountEuro, boolean receiptAttached, String description) {
        this.claimId = claimId;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.month = month;
        this.category = category;
        this.amountEuro = amountEuro;
        this.receiptAttached = receiptAttached;
        this.description = description;
        this.status = ApprovalStatus.PENDING;
    }
}
