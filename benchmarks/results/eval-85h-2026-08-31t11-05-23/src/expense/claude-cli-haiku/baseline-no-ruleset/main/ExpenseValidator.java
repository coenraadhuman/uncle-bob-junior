import java.time.YearMonth;
import java.util.*;

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
