import java.time.LocalDate;
import java.time.YearMonth;

class Expense {
    private final String employeeId;
    private final String employeeName;
    private final double amount;
    private final Category category;
    private final LocalDate date;
    private final boolean receiptAttached;
    private final String description;
    private ApprovalStatus status;
    private String rejectionReason;
    
    public Expense(String employeeId, String employeeName, double amount, 
                   Category category, LocalDate date, boolean receiptAttached,
                   String description) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.receiptAttached = receiptAttached;
        this.description = description;
        this.status = ApprovalStatus.APPROVED;
    }
    
    public String getEmployeeId() { return employeeId; }
    public String getEmployeeName() { return employeeName; }
    public double getAmount() { return amount; }
    public Category getCategory() { return category; }
    public LocalDate getDate() { return date; }
    public boolean isReceiptAttached() { return receiptAttached; }
    public String getDescription() { return description; }
    public ApprovalStatus getStatus() { return status; }
    public String getRejectionReason() { return rejectionReason; }
    
    public void setStatus(ApprovalStatus status) { this.status = status; }
    public void setRejectionReason(String reason) { this.rejectionReason = reason; }
    
    public YearMonth getYearMonth() {
        return YearMonth.from(date);
    }
}
