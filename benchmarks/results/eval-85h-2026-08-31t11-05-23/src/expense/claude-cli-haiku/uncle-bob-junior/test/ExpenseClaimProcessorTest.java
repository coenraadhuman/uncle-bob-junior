import java.time.YearMonth;

public class ExpenseClaimProcessorTest {
    private ClaimProcessor processor;
    private MonthlyReportGenerator reportGenerator;
    private YearMonth testMonth;
    
    public void setUp() {
        processor = new ClaimProcessor();
        reportGenerator = new MonthlyReportGenerator();
        testMonth = YearMonth.of(2026, 8);
    }
    
    public void testNegativeAmountRejected() {
        setUp();
        ExpenseClaim claim = new ExpenseClaim("EMP001", "John Doe", testMonth, -50, ExpenseCategory.TRAVEL, true, "Flight");
        ProcessedClaim result = processor.processClaim(claim);
        assert !result.validation().valid() : "Negative amount should be rejected";
        assert result.validation().errors().stream().anyMatch(e -> e.contains("positive")) : "Should mention positive amount";
    }
    
    public void testReceiptNotRequiredUnder25() {
        setUp();
        ExpenseClaim claim = new ExpenseClaim("EMP001", "John Doe", testMonth, 20, ExpenseCategory.MEALS, false, "Coffee");
        ProcessedClaim result = processor.processClaim(claim);
        assert result.validation().valid() : "Receipt not required under 25 euros";
    }
    
    public void testReceiptRequiredOver25() {
        setUp();
        ExpenseClaim claim = new ExpenseClaim("EMP001", "John Doe", testMonth, 50, ExpenseCategory.MEALS, false, "Lunch");
        ProcessedClaim result = processor.processClaim(claim);
        assert !result.validation().valid() : "Receipt required over 25 euros";
        assert result.validation().errors().stream().anyMatch(e -> e.contains("Receipt")) : "Should mention receipt requirement";
    }
    
    public void testMealsBudgetCap() {
        setUp();
        processor.processClaim(new ExpenseClaim("EMP002", "Jane", testMonth, 100, ExpenseCategory.MEALS, true, "Lunch"));
        ProcessedClaim result2 = processor.processClaim(new ExpenseClaim("EMP002", "Jane", testMonth, 60, ExpenseCategory.MEALS, true, "Dinner"));
        assert !result2.withinMonthlyBudget() : "Should exceed meals cap of 150";
    }
    
    public void testTravelBudgetCap() {
        setUp();
        processor.processClaim(new ExpenseClaim("EMP003", "Bob", testMonth, 400, ExpenseCategory.TRAVEL, true, "Flight"));
        ProcessedClaim result2 = processor.processClaim(new ExpenseClaim("EMP003", "Bob", testMonth, 150, ExpenseCategory.TRAVEL, true, "Hotel"));
        assert !result2.withinMonthlyBudget() : "Should exceed travel cap of 500";
    }
    
    public void testEquipmentBudgetCap() {
        setUp();
        processor.processClaim(new ExpenseClaim("EMP004", "Alice", testMonth, 800, ExpenseCategory.EQUIPMENT, true, "Laptop"));
        ProcessedClaim result2 = processor.processClaim(new ExpenseClaim("EMP004", "Alice", testMonth, 250, ExpenseCategory.EQUIPMENT, true, "Monitor"));
        assert !result2.withinMonthlyBudget() : "Should exceed equipment cap of 1000";
    }
    
    public void testAutomaticApprovalSmallClaim() {
        setUp();
        ProcessedClaim result = processor.processClaim(new ExpenseClaim("EMP001", "John", testMonth, 150, ExpenseCategory.MEALS, true, "Lunch"));
        assert result.approvalLevel() == ApprovalLevel.AUTOMATIC : "Claims under 200 euros should be automatic";
    }
    
    public void testManagerApprovalRange() {
        setUp();
        ProcessedClaim result = processor.processClaim(new ExpenseClaim("EMP001", "John", testMonth, 500, ExpenseCategory.TRAVEL, true, "Flight"));
        assert result.approvalLevel() == ApprovalLevel.MANAGER_APPROVAL : "Claims 200-1000 euros need manager approval";
    }
    
    public void testFinanceApprovalLarge() {
        setUp();
        ProcessedClaim result = processor.processClaim(new ExpenseClaim("EMP001", "John", testMonth, 1500, ExpenseCategory.EQUIPMENT, true, "Workstation"));
        assert result.approvalLevel() == ApprovalLevel.FINANCE_APPROVAL : "Claims over 1000 euros need finance approval";
    }
    
    public void testMonthlyReportGeneration() {
        setUp();
        processor.processClaim(new ExpenseClaim("EMP005", "Charlie", testMonth, 100, ExpenseCategory.MEALS, true, "Lunch"));
        processor.processClaim(new ExpenseClaim("EMP005", "Charlie", testMonth, 300, ExpenseCategory.TRAVEL, true, "Flight"));
        
        String report = reportGenerator.generateEmployeeReport("EMP005", testMonth, processor.getBudgetTracker());
        assert report.contains("EMP005") : "Report should contain employee ID";
        assert report.contains(testMonth.toString()) : "Report should contain month";
        assert report.contains("MEALS") : "Report should contain meals category";
        assert report.contains("TRAVEL") : "Report should contain travel category";
        assert report.contains("100") : "Report should show meals amount";
        assert report.contains("300") : "Report should show travel amount";
    }
    
    public static void main(String[] args) {
        ExpenseClaimProcessorTest test = new ExpenseClaimProcessorTest();
        
        test.testNegativeAmountRejected();
        test.testReceiptNotRequiredUnder25();
        test.testReceiptRequiredOver25();
        test.testMealsBudgetCap();
        test.testTravelBudgetCap();
        test.testEquipmentBudgetCap();
        test.testAutomaticApprovalSmallClaim();
        test.testManagerApprovalRange();
        test.testFinanceApprovalLarge();
        test.testMonthlyReportGeneration();
        
        System.out.println("✓ All tests passed");
        
        // Demo report
        ClaimProcessor processor = new ClaimProcessor();
        YearMonth month = YearMonth.of(2026, 8);
        processor.processClaim(new ExpenseClaim("EMP099", "Sarah Wilson", month, 80, ExpenseCategory.MEALS, true, "Team lunch"));
        processor.processClaim(new ExpenseClaim("EMP099", "Sarah Wilson", month, 450, ExpenseCategory.TRAVEL, true, "Conference flight"));
        processor.processClaim(new ExpenseClaim("EMP099", "Sarah Wilson", month, 750, ExpenseCategory.EQUIPMENT, true, "Monitor setup"));
        
        MonthlyReportGenerator generator = new MonthlyReportGenerator();
        System.out.println("\n" + generator.generateEmployeeReport("EMP099", month, processor.getBudgetTracker()));
    }
}
