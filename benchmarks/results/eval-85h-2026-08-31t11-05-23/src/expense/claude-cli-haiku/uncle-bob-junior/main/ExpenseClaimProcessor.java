import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.*;

final class ExpenseClaimProcessor {
    private final List<ExpenseClaim> allClaims;
    private final List<ExpenseClaim> validClaims;
    private final ClaimValidator validator;
    private final ApprovalRouter router;

    ExpenseClaimProcessor() {
        this.allClaims = new ArrayList<>();
        this.validClaims = new ArrayList<>();
        this.validator = new ClaimValidator();
        this.router = new ApprovalRouter();
    }

    void processClaim(ExpenseClaim claim) {
        ApprovalStatus validationResult = validator.validate(claim);
        if (validationResult == ApprovalStatus.REJECTED) {
            claim.setStatus(ApprovalStatus.REJECTED);
            allClaims.add(claim);
            return;
        }

        CategoryCapEnforcer enforcer = new CategoryCapEnforcer(validClaims);
        if (enforcer.exceedsCap(claim)) {
            claim.setStatus(ApprovalStatus.REJECTED);
            allClaims.add(claim);
            return;
        }

        ApprovalStatus approvalStatus = router.route(claim);
        claim.setStatus(approvalStatus);
        allClaims.add(claim);
        validClaims.add(claim);
    }

    String generateMonthlyReport(YearMonth month, String employeeId) {
        Map<ExpenseCategory, BigDecimal> categoryTotals = new EnumMap<>(ExpenseCategory.class);
        BigDecimal totalReimbursement = BigDecimal.ZERO;

        for (ExpenseCategory cat : ExpenseCategory.values()) {
            categoryTotals.put(cat, BigDecimal.ZERO);
        }

        for (ExpenseClaim claim : validClaims) {
            if (isInReportScope(claim, month, employeeId)) {
                BigDecimal current = categoryTotals.get(claim.category());
                categoryTotals.put(claim.category(), current.add(claim.amount()));
                totalReimbursement = totalReimbursement.add(claim.amount());
            }
        }

        return formatReport(employeeId, month, categoryTotals, totalReimbursement);
    }

    private boolean isInReportScope(ExpenseClaim claim, YearMonth month, String employeeId) {
        return claim.month().equals(month) &&
               claim.employeeId().equals(employeeId) &&
               claim.status() != ApprovalStatus.REJECTED;
    }

    private String formatReport(String employeeId, YearMonth month,
                                Map<ExpenseCategory, BigDecimal> totals,
                                BigDecimal total) {
        StringBuilder sb = new StringBuilder();
        sb.append("Monthly Reimbursement Report\n");
        sb.append("Employee: ").append(employeeId).append("\n");
        sb.append("Month: ").append(month).append("\n");
        sb.append("=".repeat(40)).append("\n");
        for (Map.Entry<ExpenseCategory, BigDecimal> entry : totals.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append(" EUR\n");
        }
        sb.append("-".repeat(40)).append("\n");
        sb.append("Total: ").append(total).append(" EUR\n");
        return sb.toString();
    }

    List<ExpenseClaim> claims() {
        return new ArrayList<>(allClaims);
    }
}
