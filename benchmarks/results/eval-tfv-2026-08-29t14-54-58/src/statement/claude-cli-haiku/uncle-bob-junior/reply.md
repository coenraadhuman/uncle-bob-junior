**Assumptions:**
- Exchange rates fixed: USD 1.0 → EUR 1.10, GBP 1.0 → EUR 1.17
- Categories detected by keywords in description (case-insensitive): "salary|wage" → SALARY, "rent" → RENT, "albert heijn|supermarket|grocery|ah" → GROCERIES
- Suspicious: amounts > 2000 EUR (absolute value) OR duplicate amounts on same day
- Input format: `date;description;amount;currency` with ISO date (YYYY-MM-DD)

```java
import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

public class BankStatementAnalyzer {
    
    enum Currency {
        EUR(BigDecimal.ONE),
        USD(BigDecimal.valueOf(1.10)),
        GBP(BigDecimal.valueOf(1.17));
        
        final BigDecimal toEur;
        
        Currency(BigDecimal toEur) {
            this.toEur = toEur;
        }
    }
    
    enum Category {
        SALARY, RENT, GROCERIES, OTHER
    }
    
    record Transaction(
        LocalDate date,
        String description,
        BigDecimal amountEur,
        Category category,
        boolean suspicious
    ) {}
    
    static class StatementParser {
        private static final String SALARY_KEYWORDS = "SALARY|WAGE";
        private static final String RENT_KEYWORDS = "RENT";
        private static final String GROCERIES_KEYWORDS = "ALBERT HEIJN|SUPERMARKET|GROCERY|AH";
        
        List<Transaction> parse(String filename) throws IOException {
            List<Transaction> transactions = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    transactions.add(parseLine(line));
                }
            }
            return transactions;
        }
        
        private Transaction parseLine(String line) {
            String[] parts = line.split(";");
            LocalDate date = LocalDate.parse(parts[0]);
            String description = parts[1];
            BigDecimal amount = new BigDecimal(parts[2]);
            Currency currency = Currency.valueOf(parts[3]);
            
            BigDecimal amountEur = amount.multiply(currency.toEur);
            Category category = categorizeDescription(description);
            
            return new Transaction(date, description, amountEur, category, false);
        }
        
        private Category categorizeDescription(String description) {
            String upper = description.toUpperCase();
            if (upper.matches(".*" + SALARY_KEYWORDS + ".*")) return Category.SALARY;
            if (upper.matches(".*" + RENT_KEYWORDS + ".*")) return Category.RENT;
            if (upper.matches(".*" + GROCERIES_KEYWORDS + ".*")) return Category.GROCERIES;
            return Category.OTHER;
        }
    }
    
    static class SuspicionChecker {
        private static final BigDecimal LARGE_THRESHOLD = BigDecimal.valueOf(2000);
        
        List<Transaction> flagSuspicious(List<Transaction> transactions) {
            Map<LocalDate, List<Transaction>> byDate = groupByDate(transactions);
            return transactions.stream()
                .map(t -> isSuspicious(t, byDate) ? withSuspiciousFlag(t) : t)
                .collect(Collectors.toList());
        }
        
        private Map<LocalDate, List<Transaction>> groupByDate(List<Transaction> transactions) {
            return transactions.stream()
                .collect(Collectors.groupingBy(Transaction::date));
        }
        
        private boolean isSuspicious(Transaction t, Map<LocalDate, List<Transaction>> byDate) {
            if (isLargeAmount(t)) return true;
            return hasDuplicateAmountSameDay(t, byDate);
        }
        
        private boolean isLargeAmount(Transaction t) {
            return t.amountEur().abs().compareTo(LARGE_THRESHOLD) > 0;
        }
        
        private boolean hasDuplicateAmountSameDay(Transaction t, Map<LocalDate, List<Transaction>> byDate) {
            long duplicateCount = byDate.get(t.date()).stream()
                .filter(other -> other.amountEur().equals(t.amountEur()))
                .count();
            return duplicateCount > 1;
        }
        
        private Transaction withSuspiciousFlag(Transaction t) {
            return new Transaction(t.date(), t.description(), t.amountEur(), t.category(), true);
        }
    }
    
    static class MonthlyReportGenerator {
        record CategoryTotal(Category category, BigDecimal total) {}
        record MonthSummary(YearMonth month, List<CategoryTotal> totals, List<Transaction> suspicious) {}
        
        List<MonthSummary> generate(List<Transaction> transactions) {
            return transactions.stream()
                .collect(Collectors.groupingBy(t -> YearMonth.from(t.date())))
                .entrySet().stream()
                .map(entry -> summarizeMonth(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(MonthSummary::month))
                .collect(Collectors.toList());
        }
        
        private MonthSummary summarizeMonth(YearMonth month, List<Transaction> monthTransactions) {
            List<CategoryTotal> totals = buildCategoryTotals(monthTransactions);
            List<Transaction> suspicious = filterSuspicious(monthTransactions);
            return new MonthSummary(month, totals, suspicious);
        }
        
        private List<CategoryTotal> buildCategoryTotals(List<Transaction> transactions) {
            return Arrays.stream(Category.values())
                .map(cat -> new CategoryTotal(cat, sumCategory(transactions, cat)))
                .collect(Collectors.toList());
        }
        
        private BigDecimal sumCategory(List<Transaction> transactions, Category category) {
            return transactions.stream()
                .filter(t -> t.category() == category)
                .map(Transaction::amountEur)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        
        private List<Transaction> filterSuspicious(List<Transaction> transactions) {
            return transactions.stream()
                .filter(Transaction::suspicious)
                .collect(Collectors.toList());
        }
    }
    
    static void printReport(List<MonthlyReportGenerator.MonthSummary> report) {
        for (var month : report) {
            System.out.println("\n=== " + month.month() + " ===");
            printCategoryTotals(month.totals());
            printSuspiciousTransactions(month.suspicious());
        }
    }
    
    private static void printCategoryTotals(List<MonthlyReportGenerator.CategoryTotal> totals) {
        for (var total : totals) {
            System.out.printf("%s: %.2f EUR\n", total.category(), total.total());
        }
    }
    
    private static void printSuspiciousTransactions(List<Transaction> suspicious) {
        if (suspicious.isEmpty()) return;
        System.out.println("\nSuspicious Transactions:");
        for (var t : suspicious) {
            System.out.printf("  %s | %s | %.2f EUR\n", t.date(), t.description(), t.amountEur());
        }
    }
    
    public static void main(String[] args) throws IOException {
        List<Transaction> transactions = new StatementParser().parse("statement.txt");
        transactions = new SuspicionChecker().flagSuspicious(transactions);
        List<MonthlyReportGenerator.MonthSummary> report = new MonthlyReportGenerator().generate(transactions);
        printReport(report);
    }
}
```

```java
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
```

Cleaned: Immutable transactions using records, currency and category enums prevent invalid states, guard clauses for empty suspicious lists, named constants for thresholds and keywords. Safe to change because responsibility boundaries isolate parsing, flagging, and reporting; streams avoid mutable intermediate collections.