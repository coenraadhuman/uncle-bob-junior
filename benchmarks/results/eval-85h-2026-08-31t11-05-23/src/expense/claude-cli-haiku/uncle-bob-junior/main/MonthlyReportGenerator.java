import java.time.YearMonth;
import java.util.*;

public class MonthlyReportGenerator {
    public String generateEmployeeReport(String employeeId, YearMonth month, MonthlyBudgetTracker tracker) {
        Map<ExpenseCategory, Integer> spending = tracker.getMonthlySpending(employeeId, month);
        
        StringBuilder report = new StringBuilder();
        report.append("=== MONTHLY EXPENSE REPORT ===\n");
        report.append("Employee: ").append(employeeId).append("\n");
        report.append("Month: ").append(month).append("\n\n");
        
        int totalSpent = 0;
        for (ExpenseCategory category : ExpenseCategory.values()) {
            int amount = spending.getOrDefault(category, 0);
            int cap = category.monthlyCapEuros();
            int remaining = cap - amount;
            totalSpent += amount;
            
            report.append(String.format("%-15s €%-6d / €%-6d (Remaining: €%d)\n", 
                category + ":", amount, cap, remaining));
        }
        
        report.append("\n");
        int totalCap = Arrays.stream(ExpenseCategory.values())
            .mapToInt(ExpenseCategory::monthlyCapEuros).sum();
        report.append(String.format("TOTAL:          €%-6d / €%-6d\n", totalSpent, totalCap));
        
        return report.toString();
    }
}
