class BankStatementAnalyzerTest {
    
    static void assertEqual(Object expected, Object actual, String test) {
        if (!expected.equals(actual)) {
            throw new AssertionError(test + " failed: expected " + expected + ", got " + actual);
        }
    }
    
    static void assertTrue(boolean condition, String test) {
        if (!condition) throw new AssertionError(test + " failed");
    }
    
    static void assertFalse(boolean condition, String test) {
        if (condition) throw new AssertionError(test + " failed");
    }
    
    void testCategoryFromSalary() {
        var parser = new BankStatementAnalyzer.StatementParser();
        var tx = parser.parseLine("2026-01-15;MONTHLY SALARY;3000.00;EUR");
        assertEqual(BankStatementAnalyzer.Category.SALARY, tx.category(), "Salary categorization");
    }
    
    void testCategoryFromRent() {
        var parser = new BankStatementAnalyzer.StatementParser();
        var tx = parser.parseLine("2026-01-01;RENT PAYMENT;1200.00;EUR");
        assertEqual(BankStatementAnalyzer.Category.RENT, tx.category(), "Rent categorization");
    }
    
    void testCategoryFromGroceries() {
        var parser = new BankStatementAnalyzer.StatementParser();
        var tx = parser.parseLine("2026-01-15;ALBERT HEIJN;-23.95;EUR");
        assertEqual(BankStatementAnalyzer.Category.GROCERIES, tx.category(), "Groceries categorization");
    }
    
    void testCategoryOther() {
        var parser = new BankStatementAnalyzer.StatementParser();
        var tx = parser.parseLine("2026-01-15;RANDOM STORE;-50.00;EUR");
        assertEqual(BankStatementAnalyzer.Category.OTHER, tx.category(), "Other categorization");
    }
    
    void testUSDConversion() {
        var parser = new BankStatementAnalyzer.StatementParser();
        var tx = parser.parseLine("2026-01-15;PAYMENT;100.00;USD");
        assertEqual(BigDecimal.valueOf(110.00), tx.amountEur(), "USD to EUR conversion");
    }
    
    void testGBPConversion() {
        var parser = new BankStatementAnalyzer.StatementParser();
        var tx = parser.parseLine("2026-01-15;PAYMENT;100.00;GBP");
        assertEqual(BigDecimal.valueOf(117.00), tx.amountEur(), "GBP to EUR conversion");
    }
    
    void testLargeAmountFlagged() {
        var checker = new BankStatementAnalyzer.SuspicionChecker();
        var tx = new BankStatementAnalyzer.Transaction(
            LocalDate.of(2026, 1, 15), "TRANSFER", BigDecimal.valueOf(2500.00),
            BankStatementAnalyzer.Category.OTHER, false
        );
        var flagged = checker.flagSuspicious(List.of(tx));
        assertTrue(flagged.get(0).suspicious(), "Large amount (>2000) flagged");
    }
    
    void testSmallAmountNotFlagged() {
        var checker = new BankStatementAnalyzer.SuspicionChecker();
        var tx = new BankStatementAnalyzer.Transaction(
            LocalDate.of(2026, 1, 15), "PAYMENT", BigDecimal.valueOf(100.00),
            BankStatementAnalyzer.Category.OTHER, false
        );
        var flagged = checker.flagSuspicious(List.of(tx));
        assertFalse(flagged.get(0).suspicious(), "Small amount not flagged");
    }
    
    void testDuplicateAmountSameDayFlagged() {
        var checker = new BankStatementAnalyzer.SuspicionChecker();
        var date = LocalDate.of(2026, 1, 15);
        var t1 = new BankStatementAnalyzer.Transaction(
            date, "TRANSFER 1", BigDecimal.valueOf(500.00), BankStatementAnalyzer.Category.OTHER, false
        );
        var t2 = new BankStatementAnalyzer.Transaction(
            date, "TRANSFER 2", BigDecimal.valueOf(500.00), BankStatementAnalyzer.Category.OTHER, false
        );
        var flagged = checker.flagSuspicious(List.of(t1, t2));
        assertTrue(flagged.get(0).suspicious(), "Duplicate amount same day flagged");
        assertTrue(flagged.get(1).suspicious(), "Duplicate amount same day flagged");
    }
    
    void testDifferentDaysNotFlagged() {
        var checker = new BankStatementAnalyzer.SuspicionChecker();
        var t1 = new BankStatementAnalyzer.Transaction(
            LocalDate.of(2026, 1, 15), "T1", BigDecimal.valueOf(500.00), BankStatementAnalyzer.Category.OTHER, false
        );
        var t2 = new BankStatementAnalyzer.Transaction(
            LocalDate.of(2026, 1, 16), "T2", BigDecimal.valueOf(500.00), BankStatementAnalyzer.Category.OTHER, false
        );
        var flagged = checker.flagSuspicious(List.of(t1, t2));
        assertFalse(flagged.get(0).suspicious(), "Same amount different days not flagged");
    }
    
    void testMonthlyGrouping() {
        var gen = new BankStatementAnalyzer.MonthlyReportGenerator();
        var t1 = new BankStatementAnalyzer.Transaction(
            LocalDate.of(2026, 1, 15), "P1", BigDecimal.valueOf(100.00), BankStatementAnalyzer.Category.SALARY, false
        );
        var t2 = new BankStatementAnalyzer.Transaction(
            LocalDate.of(2026, 2, 15), "P2", BigDecimal.valueOf(100.00), BankStatementAnalyzer.Category.SALARY, false
        );
        var report = gen.generate(List.of(t1, t2));
        assertEqual(2, report.size(), "Two months generated");
    }
    
    void testCategoryTotaling() {
        var gen = new BankStatementAnalyzer.MonthlyReportGenerator();
        var t1 = new BankStatementAnalyzer.Transaction(
            LocalDate.of(2026, 1, 15), "SALARY", BigDecimal.valueOf(3000.00),
            BankStatementAnalyzer.Category.SALARY, false
        );
        var t2 = new BankStatementAnalyzer.Transaction(
            LocalDate.of(2026, 1, 20), "RENT", BigDecimal.valueOf(1200.00),
            BankStatementAnalyzer.Category.RENT, false
        );
        var report = gen.generate(List.of(t1, t2));
        var salaryTotal = report.get(0).totals().stream()
            .filter(ct -> ct.category() == BankStatementAnalyzer.Category.SALARY)
            .findFirst().orElseThrow().total();
        assertEqual(BigDecimal.valueOf(3000.00), salaryTotal, "Salary total");
    }
    
    void runAllTests() {
        testCategoryFromSalary();
        testCategoryFromRent();
        testCategoryFromGroceries();
        testCategoryOther();
        testUSDConversion();
        testGBPConversion();
        testLargeAmountFlagged();
        testSmallAmountNotFlagged();
        testDuplicateAmountSameDayFlagged();
        testDifferentDaysNotFlagged();
        testMonthlyGrouping();
        testCategoryTotaling();
        System.out.println("All tests passed!");
    }
    
    public static void main(String[] args) {
        new BankStatementAnalyzerTest().runAllTests();
    }
}
