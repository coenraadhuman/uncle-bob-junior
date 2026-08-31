I'll assume you mean Java (despite the C# mention at the start) based on your explicit request for Java code output. I'll build a system with validation, approval routing, and monthly reporting.

**Key assumptions:**
- Expenses are processed batch-style; validation checks cumulative monthly totals
- Approval flags claims but doesn't enforce human sign-off (you'd integrate that separately)
- Reports aggregate by employee and month
- Amounts are in EUR; dates use Java's LocalDate

```java
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

enum Category {
    TRAVEL(500),
    MEALS(150),
    EQUIPMENT(1000);
    
    private final double monthlyCap;
    
    Category(double monthlyCap) {
        this.monthlyCap = monthlyCap;
    }
    
    public double getMonthlyCap() {
        return monthlyCap;
    }
}

enum ApprovalStatus {
    APPROVED,
    MANAGER_APPROVAL_NEEDED,
    FINANCE_APPROVAL_NEEDED,
    REJECTED
}

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

class ExpenseValidator {
    private final Map<String, Map<YearMonth, Map<Category, Double>>> categoryTotals;
    
    public ExpenseValidator() {
        this.categoryTotals = new HashMap<>();
    }
    
    public boolean validate(Expense expense) {
        if (expense.getAmount() <= 0) {
            expense.setRejectionReason("Amount must be positive");
            return false;
        }
        
        if (expense.getAmount() > 25 && !expense.isReceiptAttached()) {
            expense.setRejectionReason("Receipt required for amounts over 25 EUR");
            return false;
        }
        
        double cap = expense.getCategory().getMonthlyCap();
        double currentTotal = getCategoryTotal(expense.getEmployeeId(), 
                                               expense.getYearMonth(), 
                                               expense.getCategory());
        
        if (currentTotal + expense.getAmount() > cap) {
            expense.setRejectionReason(
                String.format("Monthly %s cap (%.2f EUR) exceeded", 
                    expense.getCategory(), cap)
            );
            return false;
        }
        
        return true;
    }
    
    public void recordExpense(Expense expense) {
        String empId = expense.getEmployeeId();
        YearMonth month = expense.getYearMonth();
        Category cat = expense.getCategory();
        
        categoryTotals
            .computeIfAbsent(empId, k -> new HashMap<>())
            .computeIfAbsent(month, k -> new HashMap<>())
            .merge(cat, expense.getAmount(), Double::sum);
    }
    
    private double getCategoryTotal(String employeeId, YearMonth month, Category category) {
        return categoryTotals
            .getOrDefault(employeeId, new HashMap<>())
            .getOrDefault(month, new HashMap<>())
            .getOrDefault(category, 0.0);
    }
}

class ExpenseProcessor {
    private final ExpenseValidator validator;
    private final List<Expense> allExpenses;
    
    public ExpenseProcessor() {
        this.validator = new ExpenseValidator();
        this.allExpenses = new ArrayList<>();
    }
    
    public void processExpense(Expense expense) {
        if (!validator.validate(expense)) {
            expense.setStatus(ApprovalStatus.REJECTED);
            allExpenses.add(expense);
            return;
        }
        
        if (expense.getAmount() > 1000) {
            expense.setStatus(ApprovalStatus.FINANCE_APPROVAL_NEEDED);
        } else if (expense.getAmount() > 200) {
            expense.setStatus(ApprovalStatus.MANAGER_APPROVAL_NEEDED);
        }
        
        validator.recordExpense(expense);
        allExpenses.add(expense);
    }
    
    public List<Expense> getExpenses() {
        return new ArrayList<>(allExpenses);
    }
}

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

public class ExpenseClaimSystem {
    public static void main(String[] args) {
        ExpenseProcessor processor = new ExpenseProcessor();
        
        processor.processExpense(new Expense("E001", "Alice Johnson", 45.50, 
            Category.MEALS, LocalDate.of(2026, 8, 5), true, "Team lunch"));
        
        processor.processExpense(new Expense("E001", "Alice Johnson", 350.00, 
            Category.TRAVEL, LocalDate.of(2026, 8, 10), true, "Flight to Berlin"));
        
        processor.processExpense(new Expense("E001", "Alice Johnson", 28.00, 
            Category.MEALS, LocalDate.of(2026, 8, 12), false, "Coffee - no receipt"));
        
        processor.processExpense(new Expense("E002", "Bob Smith", 120.00, 
            Category.MEALS, LocalDate.of(2026, 8, 8), true, "Client dinner"));
        
        processor.processExpense(new Expense("E002", "Bob Smith", 1200.00, 
            Category.EQUIPMENT, LocalDate.of(2026, 8, 15), true, "Monitor + dock"));
        
        processor.processExpense(new Expense("E002", "Bob Smith", -50.00, 
            Category.TRAVEL, LocalDate.of(2026, 8, 20), true, "Invalid amount"));
        
        processor.processExpense(new Expense("E001", "Alice Johnson", 150.00, 
            Category.MEALS, LocalDate.of(2026, 8, 25), true, "Over cap test"));
        
        Map<String, List<Expense>> byEmployee = processor.getExpenses().stream()
            .collect(Collectors.groupingBy(Expense::getEmployeeId));
        
        for (String empId : byEmployee.keySet()) {
            List<Expense> empExpenses = byEmployee.get(empId);
            String empName = empExpenses.get(0).getEmployeeName();
            
            Map<YearMonth, List<Expense>> byMonth = empExpenses.stream()
                .collect(Collectors.groupingBy(Expense::getYearMonth));
            
            for (YearMonth month : new TreeSet<>(byMonth.keySet())) {
                MonthlyReimbursementReport report = new MonthlyReimbursementReport(
                    empId, empName, month, byMonth.get(month));
                report.print();
            }
        }
    }
}
```

The solution validates claims in order, enforces category caps cumulatively per employee-month, routes by amount thresholds, and generates detailed monthly reports with per-category summaries and approval status tracking.