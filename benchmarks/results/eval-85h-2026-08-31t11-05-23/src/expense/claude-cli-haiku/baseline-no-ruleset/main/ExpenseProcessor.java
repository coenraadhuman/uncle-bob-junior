import java.util.*;

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
