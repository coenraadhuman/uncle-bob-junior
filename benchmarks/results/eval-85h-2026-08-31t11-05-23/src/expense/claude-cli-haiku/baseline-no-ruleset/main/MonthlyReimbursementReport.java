import java.time.YearMonth;
import java.util.*;

class MonthlyReimbursementReport {
    private final String employeeId;
    private final String employeeName;
    private final YearMonth month;
    private final List<Expense> expenses;
    
    public MonthlyReimbursementReport(String employeeId, String employeeName, 
                                     YearMonth month, List<Expense> expenses) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.month = month;
        this.expenses = expenses;
    }
    
    public void print() {
        Map<Category, Double> categoryTotals = new HashMap<>();
        double totalApproved = 0;
        int approvedCount = 0;
        int managerApprovalCount = 0;
        int financeApprovalCount = 0;
        int rejectedCount = 0;
        
        for (Expense exp : expenses) {
            if (exp.getStatus() == ApprovalStatus.REJECTED) {
                rejectedCount++;
                continue;
            }
            
            double amt = exp.getAmount();
            categoryTotals.merge(exp.getCategory(), amt, Double::sum);
            totalApproved += amt;
            
            if (exp.getStatus() == ApprovalStatus.APPROVED) {
                approvedCount++;
            } else if (exp.getStatus() == ApprovalStatus.MANAGER_APPROVAL_NEEDED) {
                managerApprovalCount++;
            } else if (exp.getStatus() == ApprovalStatus.FINANCE_APPROVAL_NEEDED) {
                financeApprovalCount++;
            }
        }
        
        System.out.println("\n" + "=".repeat(90));
        System.out.println("MONTHLY REIMBURSEMENT REPORT");
        System.out.println("=".repeat(90));
        System.out.printf("Employee: %s (%s) | Period: %s%n", employeeName, employeeId, month);
        System.out.println("-".repeat(90));
        
        System.out.println("\nCategory Totals:");
        System.out.printf("  %-12s %12s %12s %8s%n", "Category", "Amount", "Cap", "Status");
        for (Category cat : Category.values()) {
            double amount = categoryTotals.getOrDefault(cat, 0.0);
            double cap = cat.getMonthlyCap();
            String status = amount > cap ? "OVER" : "OK";
            System.out.printf("  %-12s %12.2f %12.2f %8s%n", cat, amount, cap, status);
        }
        
        System.out.println("-".repeat(90));
        System.out.printf("Total Reimbursable: %.2f EUR%n", totalApproved);
        System.out.println("-".repeat(90));
        
        System.out.println("\nExpense Details:");
        System.out.printf("  %-12s %-12s %10s %-25s %-20s %s%n", 
            "Date", "Category", "Amount", "Status", "Notes", "Description");
        
        for (Expense exp : expenses) {
            String statusStr = exp.getStatus().toString().replace("_", " ");
            String notes = exp.getRejectionReason() != null ? 
                exp.getRejectionReason().substring(0, Math.min(20, exp.getRejectionReason().length())) : 
                (exp.isReceiptAttached() ? "Receipt OK" : "");
            
            System.out.printf("  %-12s %-12s %10.2f %-25s %-20s %s%n",
                exp.getDate(), exp.getCategory(), exp.getAmount(), statusStr, notes, exp.getDescription());
        }
        
        System.out.println("-".repeat(90));
        System.out.printf("Summary: %d approved | %d mgr approval | %d fin approval | %d rejected%n%n",
            approvedCount, managerApprovalCount, financeApprovalCount, rejectedCount);
    }
}
