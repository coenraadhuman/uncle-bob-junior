I'll provide a complete Java solution that parses bank statements, converts currencies, categorizes transactions, and generates reports.

**Assumptions:**
- Fixed exchange rates: USD→EUR 0.92, GBP→EUR 1.17
- Salary keywords: "salary", "wage"
- Rent keywords: "rent"
- Groceries keywords: "groceries", "supermarket", "albert heijn"
- Large transaction: >2000 EUR
- Date format: ISO_LOCAL_DATE (YYYY-MM-DD)

```java
import java.io.*;
import java.time.*;
import java.time.format.*;
import java.util.*;

public class BankStatementAnalyzer {
    private static final double USD_TO_EUR = 0.92;
    private static final double GBP_TO_EUR = 1.17;
    private static final double LARGE_AMOUNT = 2000.0;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;
    
    static class Transaction {
        final LocalDate date;
        final String description;
        final double amountEur;
        final String category;
        
        Transaction(LocalDate date, String description, double amountEur, String category) {
            this.date = date;
            this.description = description;
            this.amountEur = amountEur;
            this.category = category;
        }
    }
    
    static double convertToEur(double amount, String currency) {
        return switch(currency) {
            case "EUR" -> amount;
            case "USD" -> amount * USD_TO_EUR;
            case "GBP" -> amount * GBP_TO_EUR;
            default -> throw new IllegalArgumentException("Unknown currency: " + currency);
        };
    }
    
    static String categorize(String description) {
        String lower = description.toLowerCase();
        if (lower.contains("salary") || lower.contains("wage")) return "salary";
        if (lower.contains("rent")) return "rent";
        if (lower.contains("groceries") || lower.contains("supermarket") || lower.contains("albert heijn")) 
            return "groceries";
        return "other";
    }
    
    static Transaction parse(String line) {
        String[] parts = line.split(";");
        LocalDate date = LocalDate.parse(parts[0], DATE_FMT);
        String desc = parts[1];
        double amt = Double.parseDouble(parts[2]);
        String curr = parts[3];
        
        return new Transaction(date, desc, convertToEur(amt, curr), categorize(desc));
    }
    
    static List<Transaction> read(String file) throws IOException {
        List<Transaction> result = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.isBlank()) result.add(parse(line));
            }
        }
        return result;
    }
    
    static void run(String file) throws IOException {
        List<Transaction> txns = read(file);
        printMonthly(groupByMonth(txns));
        printSuspicious(txns);
    }
    
    static Map<YearMonth, MonthlySummary> groupByMonth(List<Transaction> txns) {
        Map<YearMonth, MonthlySummary> result = new TreeMap<>();
        for (Transaction t : txns) {
            YearMonth m = YearMonth.from(t.date);
            result.computeIfAbsent(m, k -> new MonthlySummary()).add(t);
        }
        return result;
    }
    
    static class MonthlySummary {
        private final Map<String, Double> categoryTotals = new HashMap<>();
        
        void add(Transaction t) {
            categoryTotals.merge(t.category, t.amountEur, Double::sum);
        }
        
        double getTotal(String category) {
            return categoryTotals.getOrDefault(category, 0.0);
        }
    }
    
    static void printMonthly(Map<YearMonth, MonthlySummary> monthly) {
        System.out.println("=== MONTHLY STATEMENT ===\n");
        for (var entry : monthly.entrySet()) {
            printMonth(entry.getKey(), entry.getValue());
        }
    }
    
    static void printMonth(YearMonth month, MonthlySummary summary) {
        System.out.printf("%s%n", month);
        System.out.println("-".repeat(40));
        
        double total = 0;
        for (String cat : new String[]{"salary", "rent", "groceries", "other"}) {
            double amt = summary.getTotal(cat);
            System.out.printf("  %-12s: %10.2f EUR%n", cat, amt);
            total += amt;
        }
        System.out.printf("  %-12s: %10.2f EUR%n", "TOTAL", total);
        System.out.println();
    }
    
    static void printSuspicious(List<Transaction> txns) {
        Map<LocalDate, Map<Double, Integer>> byDateAndAmount = new HashMap<>();
        for (Transaction t : txns) {
            byDateAndAmount.computeIfAbsent(t.date, k -> new HashMap<>())
                .merge(t.amountEur, 1, Integer::sum);
        }
        
        System.out.println("=== SUSPICIOUS TRANSACTIONS ===\n");
        
        boolean found = false;
        for (Transaction t : txns) {
            List<String> reasons = flagReasons(t, byDateAndAmount);
            if (!reasons.isEmpty()) {
                found = true;
                System.out.printf("%s | %-25s | %8.2f EUR | %s%n", 
                    t.date, t.description, t.amountEur, String.join("; ", reasons));
            }
        }
        
        if (!found) System.out.println("None");
    }
    
    static List<String> flagReasons(Transaction t, Map<LocalDate, Map<Double, Integer>> counted) {
        List<String> reasons = new ArrayList<>();
        
        if (Math.abs(t.amountEur) > LARGE_AMOUNT) {
            reasons.add("amount > 2000 EUR");
        }
        
        int count = counted.getOrDefault(t.date, new HashMap<>()).getOrDefault(t.amountEur, 0);
        if (count > 1) {
            reasons.add("duplicate amount same day");
        }
        
        return reasons;
    }
    
    public static void main(String[] args) throws IOException {
        run("statement.txt");
    }
}

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
```

**How to use:**
- Create `statement.txt` with lines like: `2026-01-15;ALBERT HEIJN;-23.95;EUR`
- Run `BankStatementAnalyzer.main()` to analyze and generate the report
- Run `BankStatementAnalyzerTest.main()` to verify parsing, conversion, and categorization

**Output:**
- Month-by-month summary with category totals
- Flagged suspicious transactions (large amounts or duplicates on same day)