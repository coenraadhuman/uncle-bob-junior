import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

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
