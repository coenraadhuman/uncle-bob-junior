import java.io.*;
import java.util.*;

class BankStatementAnalyzerTest {
    private final CurrencyConverter converter = new CurrencyConverter();
    private final TransactionCategorizer categorizer = new TransactionCategorizer();

    void testCurrencyConversion() {
        assert converter.toEur(100, "EUR") == 100;
        assert Math.abs(converter.toEur(100, "USD") - 92.0) < 0.01;
        assert Math.abs(converter.toEur(100, "GBP") - 117.0) < 0.01;
        System.out.println("✓ Currency conversion tests passed");
    }

    void testCategorization() {
        assert categorizer.categorize("Monthly SALARY") == TransactionCategory.SALARY;
        assert categorizer.categorize("RENT Payment") == TransactionCategory.RENT;
        assert categorizer.categorize("ALBERT HEIJN") == TransactionCategory.GROCERIES;
        assert categorizer.categorize("Random store") == TransactionCategory.OTHER;
        System.out.println("✓ Categorization tests passed");
    }

    void testDuplicateDetection() throws IOException {
        String testFile = "test_statement.txt";
        createTestFile(testFile,
            "2026-01-15;Store A;-50.00;EUR\n" +
            "2026-01-15;Store B;-50.00;EUR\n" +
            "2026-01-16;Store C;-50.00;EUR"
        );

        BankStatementParser parser = new BankStatementParser(converter, categorizer);
        List<Transaction> transactions = parser.parse(testFile);

        assert !transactions.get(0).isSuspicious();
        assert transactions.get(1).isSuspicious();
        assert !transactions.get(2).isSuspicious();

        System.out.println("✓ Duplicate detection tests passed");
    }

    void testLargeAmountDetection() throws IOException {
        String testFile = "test_large.txt";
        createTestFile(testFile, "2026-01-15;Large Payment;-2500.00;EUR");

        BankStatementParser parser = new BankStatementParser(converter, categorizer);
        List<Transaction> transactions = parser.parse(testFile);

        assert transactions.get(0).isSuspicious();
        System.out.println("✓ Large amount detection tests passed");
    }

    private void createTestFile(String filename, String content) throws IOException {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(content);
        }
    }

    public static void main(String[] args) throws IOException {
        BankStatementAnalyzerTest test = new BankStatementAnalyzerTest();
        test.testCurrencyConversion();
        test.testCategorization();
        test.testDuplicateDetection();
        test.testLargeAmountDetection();
        System.out.println("\nAll tests passed ✓");
    }
}
