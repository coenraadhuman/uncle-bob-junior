import java.math.BigDecimal;
import java.util.*;

final class CategoryCapEnforcer {
    private static final Map<ExpenseCategory, BigDecimal> CAPS = Map.of(
        ExpenseCategory.TRAVEL, new BigDecimal("500.00"),
        ExpenseCategory.MEALS, new BigDecimal("150.00"),
        ExpenseCategory.EQUIPMENT, new BigDecimal("1000.00")
    );

    private final List<ExpenseClaim> approvedClaims;

    CategoryCapEnforcer(List<ExpenseClaim> approvedClaims) {
        this.approvedClaims = approvedClaims;
    }

    boolean exceedsCap(ExpenseClaim claim) {
        BigDecimal monthlyUsage = monthlyUsageForEmployeeCategory(claim);
        BigDecimal cap = CAPS.get(claim.category());
        return monthlyUsage.add(claim.amount()).compareTo(cap) > 0;
    }

    private BigDecimal monthlyUsageForEmployeeCategory(ExpenseClaim claim) {
        return approvedClaims.stream()
            .filter(c -> c.employeeId().equals(claim.employeeId()))
            .filter(c -> c.month().equals(claim.month()))
            .filter(c -> c.category() == claim.category())
            .map(ExpenseClaim::amount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
