public class ApprovalRouter {
    private static final int FINANCE_THRESHOLD_EUROS = 1000;
    private static final int MANAGER_THRESHOLD_EUROS = 200;
    
    public ApprovalLevel routeForApproval(ExpenseClaim claim) {
        if (claim.amountEuros() > FINANCE_THRESHOLD_EUROS) {
            return ApprovalLevel.FINANCE_APPROVAL;
        }
        if (claim.amountEuros() > MANAGER_THRESHOLD_EUROS) {
            return ApprovalLevel.MANAGER_APPROVAL;
        }
        return ApprovalLevel.AUTOMATIC;
    }
}
