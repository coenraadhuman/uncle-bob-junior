import java.time.YearMonth;

public class ExpenseClaimProcessor {
    public static void main(String[] args) {
        ExpenseProcessor processor = new ExpenseProcessor();
        YearMonth august2026 = YearMonth.of(2026, 8);

        // Submit various claims
        processor.submitClaim(new ExpenseClaim("C001", "E001", "Alice Johnson", august2026,
            Category.TRAVEL, 250, true, "Flight to Berlin"));
        processor.submitClaim(new ExpenseClaim("C002", "E001", "Alice Johnson", august2026,
            Category.TRAVEL, 180, true, "Hotel"));
        processor.submitClaim(new ExpenseClaim("C003", "E001", "Alice Johnson", august2026,
            Category.MEALS, 45, true, "Team lunch"));
        processor.submitClaim(new ExpenseClaim("C004", "E001", "Alice Johnson", august2026,
            Category.MEALS, 20, false, "Coffee (under 25 EUR)"));
        processor.submitClaim(new ExpenseClaim("C005", "E001", "Alice Johnson", august2026,
            Category.EQUIPMENT, 1500, true, "Monitor")); // Should be rejected - exceeds cap

        processor.submitClaim(new ExpenseClaim("C006", "E002", "Bob Smith", august2026,
            Category.TRAVEL, 600, true, "Cab")); // Should be rejected - over cap
        processor.submitClaim(new ExpenseClaim("C007", "E002", "Bob Smith", august2026,
            Category.MEALS, 35, false, "Dinner")); // Should be rejected - no receipt

        processor.submitClaim(new ExpenseClaim("C008", "E002", "Bob Smith", august2026,
            Category.EQUIPMENT, 950, true, "Laptop"));

        // Approve routed claims
        processor.approveClaim("C001", ApprovalStatus.MANAGER_APPROVED);
        processor.approveClaim("C008", ApprovalStatus.FINANCE_APPROVED);

        // Generate reports
        processor.printMonthlyReport("E001", august2026);
        processor.printMonthlyReport("E002", august2026);
    }
}
