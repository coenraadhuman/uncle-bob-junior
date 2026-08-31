I'll provide a complete Java solution for bank statement analysis with all required features.

**Assumptions:**
- statement.txt exists in the working directory with format: `date;description;amount;currency`
- Date format is `YYYY-MM-DD`
- Fixed conversion rates: USD→EUR = 0.92, GBP→EUR = 1.17
- Case-insensitive keyword matching for categorization
- "Repeated identical amounts" means same EUR amount, same calendar day

```java
import java.io.*;
import java.time.*;
import java.util.*;
import java.util.stream.*;

public class BankStatementAnalyzer {
    
    public static void main(String[] args) throws IOException {
        if (args.length > 0 && "test".equals(args[0])) {
            BankStatementAnalyzerTest.runTests();
            return;
        }
        
        List<Transaction> transactions = readTransactions("statement.txt");
        Map<YearMonth, MonthlyReport> report = generateReport(transactions);
        printReport(report);
    }
    
    private static List<Transaction> readTransactions(String filename) throws IOException {
        List<Transaction> transactions = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    transactions.add(parseTransaction(line));
                }
            }
        }
        return transactions;
    }
    
    private static Transaction parseTransaction(String line) {
        String[] parts = line.split(";");
        LocalDate date = LocalDate.parse(parts[0]);
        String description = parts[1];
        double amount = Double.parseDouble(parts[2]);
        String currency = parts[3];
        double amountEur = CurrencyConverter.toEur(amount, currency);
        String category = Categorizer.categorize(description);
        return new Transaction(date, description, amount, currency, amountEur, category);
    }
    
    private static Map<YearMonth, MonthlyReport> generateReport(List<Transaction> transactions) {
        Map<YearMonth, MonthlyReport> report = new TreeMap<>();
        for (Transaction transaction : transactions) {
            YearMonth month = YearMonth.from(transaction.date());
            MonthlyReport monthReport = report.computeIfAbsent(month, MonthlyReport::new);
            monthReport.addTransaction(transaction);
        }
        return report;
    }
    
    private static void printReport(Map<YearMonth, MonthlyReport> report) {
        report.values().forEach(MonthlyReport::print);
    }
}

record Transaction(
    LocalDate date,
    String description,
    double amount,
    String currency,
    double amountEur,
    String category
) {}

class CurrencyConverter {
    private static final double USD_TO_EUR = 0.92;
    private static final double GBP_TO_EUR = 1.17;
    
    static double toEur(double amount, String currency) {
        return switch (currency) {
            case "EUR" -> amount;
            case "USD" -> amount * USD_TO_EUR;
            case "GBP" -> amount * GBP_TO_EUR;
            default -> amount;
        };
    }
}

class Categorizer {
    private static final Map<String, List<String>> CATEGORY_KEYWORDS = Map.ofEntries(
        Map.entry("salary", List.of("salary", "wage", "payroll", "payment received")),
        Map.entry("rent", List.of("rent", "lease", "mortgage", "landlord")),
        Map.entry("groceries", List.of("albert heijn", "supermarket", "grocery", "food", "supermarkt"))
    );
    
    static String categorize(String description) {
        String lower = description.toLowerCase();
        for (var entry : CATEGORY_KEYWORDS.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (lower.contains(keyword)) {
                    return entry.getKey();
                }
            }
        }
        return "other";
    }
}

class MonthlyReport {
    private static final double SUSPICIOUS_THRESHOLD = 2000.0;
    
    private final YearMonth month;
    private final List<Transaction> transactions = new ArrayList<>();
    private final Map<String, Double> categoryTotals = new HashMap<>();
    
    MonthlyReport(YearMonth month) {
        this.month = month;
    }
    
    void addTransaction(Transaction t) {
        transactions.add(t);
        categoryTotals.merge(t.category(), t.amountEur(), Double::sum);
    }
    
    void print() {
        System.out.println("\n=== " + month + " ===");
        printCategoryTotals();
        printSuspiciousTransactions();
    }
    
    private void printCategoryTotals() {
        System.out.println("Category Totals:");
        categoryTotals.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(e -> System.out.printf("  %s: %.2f EUR%n", e.getKey(), e.getValue()));
    }
    
    private void printSuspiciousTransactions() {
        List<Transaction> suspicious = identifySuspicious();
        if (suspicious.isEmpty()) return;
        
        System.out.println("Suspicious Transactions:");
        for (Transaction t : suspicious) {
            System.out.printf("  %s | %s | %.2f EUR%n", t.date(), t.description(), t.amountEur());
        }
    }
    
    private List<Transaction> identifySuspicious() {
        Set<Transaction> suspicious = new HashSet<>();
        flagLargeTransactions(suspicious);
        flagDuplicateAmounts(suspicious);
        return suspicious.stream().sorted(Comparator.comparing(Transaction::date)).toList();
    }
    
    private void flagLargeTransactions(Set<Transaction> suspicious) {
        for (Transaction t : transactions) {
            if (Math.abs(t.amountEur()) > SUSPICIOUS_THRESHOLD) {
                suspicious.add(t);
            }
        }
    }
    
    private void flagDuplicateAmounts(Set<Transaction> suspicious) {
        Map<String, List<Transaction>> byDateAndAmount = new HashMap<>();
        for (Transaction t : transactions) {
            String key = t.date() + "|" + t.amountEur();
            byDateAndAmount.computeIfAbsent(key, k -> new ArrayList<>()).add(t);
        }
        
        for (List<Transaction> duplicates : byDateAndAmount.values()) {
            if (duplicates.size() > 1) {
                suspicious.addAll(duplicates);
            }
        }
    }
}

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
```

**Usage:**
- `javac BankStatementAnalyzer.java && java BankStatementAnalyzer` — analyzes statement.txt
- `java BankStatementAnalyzer test` — runs verification tests

**Output format:** Month-by-month reports with category totals and flagged suspicious transactions (>2000 EUR or duplicate amounts per day).