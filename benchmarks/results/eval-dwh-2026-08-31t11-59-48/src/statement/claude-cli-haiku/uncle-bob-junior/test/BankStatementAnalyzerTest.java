import java.time.*;
import java.util.*;

class BankStatementAnalyzerTest {
    static void testCurrencyConversion() {
        assert Math.abs(BankStatementAnalyzer.convertToEur(100, "EUR") - 100) < 0.01;
        assert Math.abs(BankStatementAnalyzer.convertToEur(100, "USD") - 92) < 0.01;
        assert Math.abs(BankStatementAnalyzer.convertToEur(100, "GBP") - 117) < 0.01;
        System.out.println("✓ Currency conversion");
    }
    
    static void testCategorization() {
        assert BankStatementAnalyzer.categorize("Monthly salary").equals("salary");
        assert BankStatementAnalyzer.categorize("Rent payment").equals("rent");
        assert BankStatementAnalyzer.categorize("Albert Heijn").equals("groceries");
        assert BankStatementAnalyzer.categorize("Random purchase").equals("other");
        System.out.println("✓ Categorization");
    }
    
    static void testParsing() {
        var txn = BankStatementAnalyzer.parse("2026-01-15;ALBERT HEIJN;-23.95;EUR");
        assert txn.date.equals(LocalDate.of(2026, 1, 15));
        assert txn.description.equals("ALBERT HEIJN");
        assert Math.abs(txn.amountEur - (-23.95)) < 0.01;
        assert txn.category.equals("groceries");
        System.out.println("✓ Parsing");
    }
    
    static void testUsdConversionInParsing() {
        var txn = BankStatementAnalyzer.parse("2026-01-15;Expense;-100;USD");
        assert Math.abs(txn.amountEur - (-92)) < 0.01;
        System.out.println("✓ USD conversion in parsing");
    }
    
    static void testMonthlyGrouping() {
        var txn1 = new BankStatementAnalyzer.Transaction(LocalDate.of(2026, 1, 15), "Test", 100, "other");
        var txn2 = new BankStatementAnalyzer.Transaction(LocalDate.of(2026, 1, 20), "Test2", 50, "other");
        List<BankStatementAnalyzer.Transaction> txns = Arrays.asList(txn1, txn2);
        
        var grouped = BankStatementAnalyzer.groupByMonth(txns);
        assert grouped.size() == 1;
        assert Math.abs(grouped.get(YearMonth.of(2026, 1)).getTotal("other") - 150) < 0.01;
        System.out.println("✓ Monthly grouping");
    }
    
    public static void runAll() {
        testCurrencyConversion();
        testCategorization();
        testParsing();
        testUsdConversionInParsing();
        testMonthlyGrouping();
        System.out.println("\n✓ All tests passed!");
    }
    
    public static void main(String[] args) {
        runAll();
    }
}
