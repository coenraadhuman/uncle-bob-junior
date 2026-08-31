public class ClaimProcessor {
    private final ClaimValidator validator;
    private final ApprovalRouter router;
    private final MonthlyBudgetTracker budgetTracker;
    
    public ClaimProcessor() {
        this.validator = new ClaimValidator();
        this.router = new ApprovalRouter();
        this.budgetTracker = new MonthlyBudgetTracker();
    }
    
    public ProcessedClaim processClaim(ExpenseClaim claim) {
        ClaimValidation validation = validator.validate(claim);
        
        if (!validation.valid()) {
            return new ProcessedClaim(claim, validation, ApprovalLevel.AUTOMATIC, false);
        }
        
        boolean withinBudget = budgetTracker.canAddClaim(claim);
        
        if (withinBudget) {
            budgetTracker.recordClaim(claim);
        }
        
        ApprovalLevel approvalLevel = router.routeForApproval(claim);
        
        return new ProcessedClaim(claim, validation, approvalLevel, withinBudget);
    }
    
    public MonthlyBudgetTracker getBudgetTracker() {
        return budgetTracker;
    }
}
