import java.time.LocalDate;
import java.util.*;

class BankStatementAnalyzerTest {
    public static void main(String[] args) {
        testCurrencyConversion();
        testCategorization();
        testSuspiciousDetection();
        System.out.println("All tests passed.");
    }
    
    static void testCurrencyConversion() {
        CurrencyConverter conv = new CurrencyConverter();
        
        assert Math.abs(conv.toEur(100, "EUR") - 100.0) < 0.01;
        assert Math.abs(conv.toEur(100, "USD") - 92.0) < 0.01;
        assert Math.abs(conv.toEur(100, "GBP") - 117.0) < 0.01;
    }
    
    static void testCategorization() {
        TransactionCategorizer cat = new TransactionCategorizer();
        
        assert cat.categorize("SALARY DEPOSIT").equals("salary");
        assert cat.categorize("Monthly Rent Payment").equals("rent");
        assert cat.categorize("ALBERT HEIJN SUPERMARKET").equals("groceries");
        assert cat.categorize("Unknown Transaction").equals("other");
        assert cat.categorize("grocery store visit").equals("groceries");
    }
    
    static void testSuspiciousDetection() {
        List<Transaction> trans = List.of(
            new Transaction(LocalDate.of(2026, 1, 15), "desc1", 1500.0, "other", false),
            new Transaction(LocalDate.of(2026, 1, 15), "desc2", 2500.0, "other", false),
            new Transaction(LocalDate.of(2026, 1, 16), "desc3", 500.0, "other", false),
            new Transaction(LocalDate.of(2026, 1, 16), "desc4", 500.0, "other", false)
        );
        
        SuspiciousDetector detector = new SuspiciousDetector();
        List<Transaction> flagged = detector.flag(trans);
        
        assert flagged.get(1).suspicious : "2500 EUR should be flagged (above threshold)";
        assert flagged.get(2).suspicious : "500 EUR on 1/16 duplicated, should be flagged";
        assert flagged.get(3).suspicious : "500 EUR on 1/16 duplicated, should be flagged";
        assert !flagged.get(0).suspicious : "1500 EUR unique, should not be flagged";
    }
}
