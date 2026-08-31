class BankStatementAnalyzerTest {
    
    static void runTests() {
        testCurrencyConversion();
        testCategorization();
        System.out.println("✓ All tests passed");
    }
    
    private static void testCurrencyConversion() {
        assert CurrencyConverter.toEur(100, "EUR") == 100;
        assert Math.abs(CurrencyConverter.toEur(100, "USD") - 92.0) < 0.01;
        assert Math.abs(CurrencyConverter.toEur(100, "GBP") - 117.0) < 0.01;
        assert CurrencyConverter.toEur(-50, "EUR") == -50;
        assert Math.abs(CurrencyConverter.toEur(-100, "USD") + 92.0) < 0.01;
    }
    
    private static void testCategorization() {
        assert "salary".equals(Categorizer.categorize("Monthly salary payment"));
        assert "groceries".equals(Categorizer.categorize("ALBERT HEIJN"));
        assert "rent".equals(Categorizer.categorize("Rent payment to landlord"));
        assert "other".equals(Categorizer.categorize("Insurance premium"));
        assert "groceries".equals(Categorizer.categorize("supermarket food purchase"));
    }
}
