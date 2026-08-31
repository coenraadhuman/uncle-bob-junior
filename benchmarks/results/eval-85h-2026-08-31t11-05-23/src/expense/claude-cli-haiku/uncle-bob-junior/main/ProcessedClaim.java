public record ProcessedClaim(
    ExpenseClaim claim,
    ClaimValidation validation,
    ApprovalLevel approvalLevel,
    boolean withinMonthlyBudget
) {}
