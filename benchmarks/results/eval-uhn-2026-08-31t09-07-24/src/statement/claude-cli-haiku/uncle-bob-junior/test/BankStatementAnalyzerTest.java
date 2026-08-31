import java.time.LocalDate;
import java.time.YearMonth;
import java.math.BigDecimal;
import java.util.*;

class BankStatementAnalyzerTest {
    public static void main(String[] args) {
        testParseTransaction();
        testCurrencyConversion();
        testCategorization();
        testSuspiciousAboveThreshold();
        testSuspiciousRepeatedAmount();
        System.out.println("All tests passed.");
    }

    static void testParseTransaction() {
        var tx = BankStatementAnalyzer.parseLine("2026-01-15;ALBERT HEIJN;-23.95;EUR");
        assert tx != null && tx.date.equals(LocalDate.of(2026, 1, 15));
        assert tx.amountEur.equals(new BigDecimal("-23.95"));
        assert tx.category == BankStatementAnalyzer.Category.GROCERIES;
    }

    static void testCurrencyConversion() {
        assert BankStatementAnalyzer.convertToEur(new BigDecimal("100"), "USD")
            .equals(new BigDecimal("92.00"));
        assert BankStatementAnalyzer.convertToEur(new BigDecimal("100"), "GBP")
            .equals(new BigDecimal("127.00"));
    }

    static void testCategorization() {
        assert BankStatementAnalyzer.categorize("SALARY PAYMENT") == BankStatementAnalyzer.Category.SALARY;
        assert BankStatementAnalyzer.categorize("RENT PAYMENT") == BankStatementAnalyzer.Category.RENT;
        assert BankStatementAnalyzer.categorize("ALBERT HEIJN") == BankStatementAnalyzer.Category.GROCERIES;
        assert BankStatementAnalyzer.categorize("MISC") == BankStatementAnalyzer.Category.OTHER;
    }

    static void testSuspiciousAboveThreshold() {
        var tx = new BankStatementAnalyzer.Transaction(LocalDate.now(), "Transfer", new BigDecimal("2500"), "EUR", BankStatementAnalyzer.Category.OTHER);
        assert BankStatementAnalyzer.isAboveThreshold(tx);
    }

    static void testSuspiciousRepeatedAmount() {
        LocalDate day = LocalDate.of(2026, 1, 15);
        var tx1 = new BankStatementAnalyzer.Transaction(day, "Payment A", new BigDecimal("100"), "EUR", BankStatementAnalyzer.Category.OTHER);
        var tx2 = new BankStatementAnalyzer.Transaction(day, "Payment B", new BigDecimal("100"), "EUR", BankStatementAnalyzer.Category.OTHER);
        var txs = List.of(tx1, tx2);
        assert BankStatementAnalyzer.isRepeatedSameDay(tx1, txs);
    }
}
