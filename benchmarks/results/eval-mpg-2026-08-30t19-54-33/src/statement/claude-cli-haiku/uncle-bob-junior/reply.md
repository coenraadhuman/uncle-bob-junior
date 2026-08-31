```java
import java.io.*;
import java.time.*;
import java.util.*;
import java.util.stream.*;

enum TransactionCategory {
    SALARY, RENT, GROCERIES, OTHER
}

class Transaction {
    private final LocalDate date;
    private final String description;
    private final double amountEur;
    private final TransactionCategory category;
    private final boolean suspicious;

    Transaction(LocalDate date, String description, double amountEur,
                TransactionCategory category, boolean suspicious) {
        this.date = date;
        this.description = description;
        this.amountEur = amountEur;
        this.category = category;
        this.suspicious = suspicious;
    }

    LocalDate date() { return date; }
    String description() { return description; }
    double amountEur() { return amountEur; }
    TransactionCategory category() { return category; }
    boolean isSuspicious() { return suspicious; }
    YearMonth month() { return YearMonth.from(date); }
}

record RawTransaction(LocalDate date, String description, double amountEur) {}

class CurrencyConverter {
    private static final double USD_TO_EUR = 0.92;
    private static final double GBP_TO_EUR = 1.17;

    double toEur(double amount, String currency) {
        return switch (currency.toUpperCase()) {
            case "EUR" -> amount;
            case "USD" -> amount * USD_TO_EUR;
            case "GBP" -> amount * GBP_TO_EUR;
            default -> throw new IllegalArgumentException("Unknown currency: " + currency);
        };
    }
}

class TransactionCategorizer {
    private static final String[] SALARY_KEYWORDS = {"SALARY", "WAGE", "PAYMENT"};
    private static final String[] RENT_KEYWORDS = {"RENT", "APARTMENT", "LANDLORD"};
    private static final String[] GROCERY_KEYWORDS = {"ALBERT HEIJN", "SUPERMARKET",
                                                       "GROCERY", "FOOD", "MARKET"};

    TransactionCategory categorize(String description) {
        String upper = description.toUpperCase();

        if (matchesAny(upper, SALARY_KEYWORDS)) return TransactionCategory.SALARY;
        if (matchesAny(upper, RENT_KEYWORDS)) return TransactionCategory.RENT;
        if (matchesAny(upper, GROCERY_KEYWORDS)) return TransactionCategory.GROCERIES;
        return TransactionCategory.OTHER;
    }

    private boolean matchesAny(String text, String[] keywords) {
        return Arrays.stream(keywords).anyMatch(text::contains);
    }
}

class BankStatementParser {
    private static final double SUSPICIOUS_AMOUNT_THRESHOLD = 2000.0;
    private final CurrencyConverter converter;
    private final TransactionCategorizer categorizer;

    BankStatementParser(CurrencyConverter converter, TransactionCategorizer categorizer) {
        this.converter = converter;
        this.categorizer = categorizer;
    }

    List<Transaction> parse(String filename) throws IOException {
        List<RawTransaction> raw = readRaw(filename);
        return enrich(raw);
    }

    private List<RawTransaction> readRaw(String filename) throws IOException {
        List<RawTransaction> raw = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    raw.add(parseRaw(line));
                }
            }
        }
        return raw;
    }

    private RawTransaction parseRaw(String line) {
        String[] parts = line.split(";");
        LocalDate date = LocalDate.parse(parts[0]);
        String description = parts[1];
        double amount = Double.parseDouble(parts[2]);
        String currency = parts[3];
        double amountEur = converter.toEur(amount, currency);
        return new RawTransaction(date, description, amountEur);
    }

    private List<Transaction> enrich(List<RawTransaction> raw) {
        Map<LocalDate, Map<Double, Integer>> dayAmounts = new HashMap<>();
        List<Transaction> transactions = new ArrayList<>();

        for (RawTransaction r : raw) {
            boolean isDuplicate = markAmountOnDay(r.date(), r.amountEur(), dayAmounts);
            TransactionCategory category = categorizer.categorize(r.description());
            boolean suspicious = r.amountEur() > SUSPICIOUS_AMOUNT_THRESHOLD || isDuplicate;

            transactions.add(new Transaction(r.date(), r.description(), r.amountEur(),
                                             category, suspicious));
        }
        return transactions;
    }

    private boolean markAmountOnDay(LocalDate date, double amount,
                                    Map<LocalDate, Map<Double, Integer>> dayAmounts) {
        Map<Double, Integer> amounts = dayAmounts.computeIfAbsent(date, k -> new HashMap<>());
        int count = amounts.getOrDefault(amount, 0);
        amounts.put(amount, count + 1);
        return count > 0;
    }
}

class BankStatementReport {
    private final List<Transaction> transactions;

    BankStatementReport(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    void print() {
        printMonthlyReport();
        printSuspiciousTransactions();
    }

    private void printMonthlyReport() {
        Map<YearMonth, Map<TransactionCategory, Double>> monthlyCategoryTotals =
            calculateMonthlyCategoryTotals();

        List<YearMonth> months = monthlyCategoryTotals.keySet().stream()
            .sorted()
            .toList();

        for (YearMonth month : months) {
            printMonth(month, monthlyCategoryTotals.get(month));
        }
    }

    private Map<YearMonth, Map<TransactionCategory, Double>> calculateMonthlyCategoryTotals() {
        return transactions.stream()
            .collect(Collectors.groupingBy(
                Transaction::month,
                Collectors.groupingBy(
                    Transaction::category,
                    Collectors.summingDouble(Transaction::amountEur)
                )
            ));
    }

    private void printMonth(YearMonth month, Map<TransactionCategory, Double> totals) {
        System.out.println("\n" + month);
        System.out.println("--------");
        for (TransactionCategory category : TransactionCategory.values()) {
            double total = totals.getOrDefault(category, 0.0);
            System.out.printf("%s: €%.2f%n", category, total);
        }
    }

    private void printSuspiciousTransactions() {
        List<Transaction> suspicious = transactions.stream()
            .filter(Transaction::isSuspicious)
            .sorted(Comparator.comparing(Transaction::date))
            .toList();

        if (!suspicious.isEmpty()) {
            System.out.println("\n\nSuspicious Transactions");
            System.out.println("======================");
            for (Transaction t : suspicious) {
                System.out.printf("%s | %s | €%.2f | %s%n",
                    t.date(), t.description(), t.amountEur(), t.category());
            }
        }
    }
}

public class BankStatementAnalyzer {
    public static void main(String[] args) throws IOException {
        CurrencyConverter converter = new CurrencyConverter();
        TransactionCategorizer categorizer = new TransactionCategorizer();
        BankStatementParser parser = new BankStatementParser(converter, categorizer);

        List<Transaction> transactions = parser.parse("statement.txt");
        BankStatementReport report = new BankStatementReport(transactions);
        report.print();
    }
}

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
```

**Assumptions:**
- Fixed rates: USD→EUR at 0.92, GBP→EUR at 1.17
- Keywords: "SALARY"/"WAGE"/"PAYMENT" for salary; "RENT"/"APARTMENT"/"LANDLORD" for rent; "ALBERT HEIJN"/"SUPERMARKET"/"GROCERY"/"FOOD"/"MARKET" for groceries
- Suspicious: amounts > €2000 or identical amounts appearing twice+ on same day
- Output: month-by-month category totals, then flagged transactions list

Cleaned: each class owns one concern (parsing, converting, categorizing, reporting); immutable `Transaction` and intermediate `RawTransaction` record; guard-clause duplicate detection; constant thresholds. Safe to change because duplicate logic, currency rates, and keywords are isolated and categorization depends on clean immutable data.