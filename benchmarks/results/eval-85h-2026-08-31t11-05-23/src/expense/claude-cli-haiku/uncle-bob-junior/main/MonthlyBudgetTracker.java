import java.time.YearMonth;
import java.util.*;

public class MonthlyBudgetTracker {
    private final Map<String, Map<YearMonth, Map<ExpenseCategory, Integer>>> spending;
    
    public MonthlyBudgetTracker() {
        this.spending = new HashMap<>();
    }
    
    public boolean canAddClaim(ExpenseClaim claim) {
        int current = currentSpending(claim.employeeId(), claim.month(), claim.category());
        return current + claim.amountEuros() <= claim.category().monthlyCapEuros();
    }
    
    public void recordClaim(ExpenseClaim claim) {
        spending
            .computeIfAbsent(claim.employeeId(), k -> new HashMap<>())
            .computeIfAbsent(claim.month(), k -> new HashMap<>())
            .merge(claim.category(), claim.amountEuros(), Integer::sum);
    }
    
    private int currentSpending(String employeeId, YearMonth month, ExpenseCategory category) {
        return spending
            .getOrDefault(employeeId, new HashMap<>())
            .getOrDefault(month, new HashMap<>())
            .getOrDefault(category, 0);
    }
    
    public Map<ExpenseCategory, Integer> getMonthlySpending(String employeeId, YearMonth month) {
        return spending
            .getOrDefault(employeeId, new HashMap<>())
            .getOrDefault(month, new HashMap<>());
    }
}
