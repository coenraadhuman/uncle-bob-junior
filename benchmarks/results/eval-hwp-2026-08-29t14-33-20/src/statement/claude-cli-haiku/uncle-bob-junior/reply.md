I'll create a complete Java solution for bank statement analysis. **Assumptions**: USD→EUR rate 0.92, GBP→EUR rate 1.17; input file is `statement.txt`; date format YYYY-MM-DD; categorization by keywords in description; floating-point amounts compared with ±0.01 tolerance.

```java
import java.io.*;
import java.time.*;
import java.util.*;
import java.util.stream.*;

public class BankStatementAnalyzer {
    public static void main(String[] args) throws IOException {
        new BankStatementAnalyzer().run();
    }
    
    private void run() throws IOException {
        var transactions = readTransactions();
        var analyzed = analyzeTransactions(transactions);
        new MonthlyReporter().report(analyzed);
    }
    
    private List<String> readTransactions() throws IOException {
        try (var reader = new BufferedReader(new FileReader("statement.txt"))) {
            return reader.lines()
                .filter(line -> !line.trim().isEmpty())
                .collect(Collectors.toList());
        }
    }
    
    private List<Transaction> analyzeTransactions(List<String> lines) {
        var parsed = lines.stream().map(Transaction::parse).collect(Collectors.toList());
        return new SuspiciousFlagService().flagAll(parsed);
    }
}

record Transaction(
    LocalDate date,
    String description,
    double amount,
    String currency,
    double amountEur,
    String category,
    boolean suspicious
) {
    static Transaction parse(String line) {
        var parts = line.split(";");
        if (parts.length != 4) throw new IllegalArgumentException("Invalid: " + line);
        var date = LocalDate.parse(parts[0]);
        var desc = parts[1].trim();
        var amt = Double.parseDouble(parts[2]);
        var curr = parts[3].trim();
        var eurAmt = convertToEur(amt, curr);
        var cat = categorizeTransaction(desc);
        return new Transaction(date, desc, amt, curr, eurAmt, cat, false);
    }
    
    private static double convertToEur(double amount, String currency) {
        return switch (currency) {
            case "USD" -> amount * 0.92;
            case "GBP" -> amount * 1.17;
            case "EUR" -> amount;
            default -> throw new IllegalArgumentException("Unknown: " + currency);
        };
    }
    
    private static String categorizeTransaction(String description) {
        var lower = description.toLowerCase();
        if (hasAny(lower, "salary", "wage", "payment", "deposit")) return "salary";
        if (hasAny(lower, "rent", "landlord", "lease")) return "rent";
        if (hasAny(lower, "albert heijn", "supermarket", "grocery", "food", "market")) return "groceries";
        return "other";
    }
    
    private static boolean hasAny(String text, String... keywords) {
        return Arrays.stream(keywords).anyMatch(text::contains);
    }
    
    Transaction withSuspicious() {
        return new Transaction(date, description, amount, currency, amountEur, category, true);
    }
}

class SuspiciousFlagService {
    private static final double LARGE_AMOUNT = 2000.0;
    
    List<Transaction> flagAll(List<Transaction> transactions) {
        var flaggedLarge = flagLargeAmounts(transactions);
        return flagDuplicateDayAmounts(flaggedLarge);
    }
    
    private List<Transaction> flagLargeAmounts(List<Transaction> transactions) {
        return transactions.stream()
            .map(t -> Math.abs(t.amountEur()) > LARGE_AMOUNT ? t.withSuspicious() : t)
            .collect(Collectors.toList());
    }
    
    private List<Transaction> flagDuplicateDayAmounts(List<Transaction> transactions) {
        var duplicates = findDuplicateDayAmounts(transactions);
        return transactions.stream()
            .map(t -> duplicates.contains(dateAmountKey(t)) ? t.withSuspicious() : t)
            .collect(Collectors.toList());
    }
    
    private Set<DateAmountKey> findDuplicateDayAmounts(List<Transaction> transactions) {
        return transactions.stream()
            .collect(Collectors.groupingBy(this::dateAmountKey))
            .entrySet().stream()
            .filter(e -> e.getValue().size() > 1)
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());
    }
    
    private DateAmountKey dateAmountKey(Transaction t) {
        return new DateAmountKey(t.date(), t.amountEur());
    }
}

record DateAmountKey(LocalDate date, double amount) {
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DateAmountKey k)) return false;
        return date.equals(k.date) && Math.abs(amount - k.amount) < 0.01;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(date, Math.round(amount * 100));
    }
}

class MonthlyReporter {
    void report(List<Transaction> transactions) {
        groupByMonth(transactions).entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(this::printMonth);
    }
    
    private Map<String, List<Transaction>> groupByMonth(List<Transaction> transactions) {
        return transactions.stream()
            .collect(Collectors.groupingBy(this::monthKey));
    }
    
    private void printMonth(Map.Entry<String, List<Transaction>> entry) {
        System.out.println("\n=== " + entry.getKey() + " ===");
        printCategories(entry.getValue());
        printSuspicious(entry.getValue());
    }
    
    private void printCategories(List<Transaction> transactions) {
        var totals = transactions.stream()
            .collect(Collectors.groupingBy(
                Transaction::category,
                Collectors.summingDouble(Transaction::amountEur)
            ));
        totals.forEach((cat, total) -> 
            System.out.printf("%s: %.2f EUR\n", cat, total));
    }
    
    private void printSuspicious(List<Transaction> transactions) {
        var flagged = transactions.stream()
            .filter(Transaction::suspicious)
            .collect(Collectors.toList());
        
        if (flagged.isEmpty()) return;
        
        System.out.println("\nSuspicious transactions:");
        flagged.forEach(t -> 
            System.out.printf("  %s | %s | %.2f EUR\n", t.date(), t.description(), t.amountEur()));
    }
    
    private String monthKey(Transaction t) {
        return String.format("%d-%02d", t.date().getYear(), t.date().getMonthValue());
    }
}

class BankStatementAnalyzerTest {
    public static void main(String[] args) {
        testTransactionParsing();
        testCurrencyConversion();
        testCategorization();
        testLargeAmountFlag();
        testDuplicateAmountFlag();
        System.out.println("✓ All tests passed");
    }
    
    private static void testTransactionParsing() {
        var t = Transaction.parse("2026-01-15;ALBERT HEIJN;-23.95;EUR");
        assert t.date().equals(LocalDate.of(2026, 1, 15));
        assert t.description().equals("ALBERT HEIJN");
        assert t.amount() == -23.95;
        assert t.currency().equals("EUR");
        assert Math.abs(t.amountEur() - (-23.95)) < 0.01;
    }
    
    private static void testCurrencyConversion() {
        var eur = Transaction.parse("2026-01-01;test;100;EUR");
        var usd = Transaction.parse("2026-01-01;test;100;USD");
        var gbp = Transaction.parse("2026-01-01;test;100;GBP");
        
        assert Math.abs(eur.amountEur() - 100) < 0.01;
        assert Math.abs(usd.amountEur() - 92) < 0.01;
        assert Math.abs(gbp.amountEur() - 117) < 0.01;
    }
    
    private static void testCategorization() {
        assert Transaction.parse("2026-01-01;Monthly Salary;100;EUR").category().equals("salary");
        assert Transaction.parse("2026-01-01;Rent Payment;-800;EUR").category().equals("rent");
        assert Transaction.parse("2026-01-01;ALBERT HEIJN;-50;EUR").category().equals("groceries");
        assert Transaction.parse("2026-01-01;Mystery;100;EUR").category().equals("other");
    }
    
    private static void testLargeAmountFlag() {
        var normal = Transaction.parse("2026-01-01;normal;1500;EUR");
        var large = Transaction.parse("2026-01-01;large;2500;EUR");
        
        var flagged = new SuspiciousFlagService().flagAll(List.of(normal, large));
        
        assert !flagged.get(0).suspicious();
        assert flagged.get(1).suspicious();
    }
    
    private static void testDuplicateAmountFlag() {
        var t1 = Transaction.parse("2026-01-01;shop1;-50;EUR");
        var t2 = Transaction.parse("2026-01-01;shop2;-50;EUR");
        var t3 = Transaction.parse("2026-01-02;shop3;-50;EUR");
        
        var flagged = new SuspiciousFlagService().flagAll(List.of(t1, t2, t3));
        
        assert flagged.get(0).suspicious();
        assert flagged.get(1).suspicious();
        assert !flagged.get(2).suspicious();
    }
}
```

**Key design**: Single `Transaction` immutable record holds parsed and converted data. `SuspiciousFlagService` identifies both large amounts and same-day duplicate amounts. `MonthlyReporter` groups by month, totals by category, and prints suspicious items. Tests cover parsing, currency conversion, categorization, and both flagging rules. Run `BankStatementAnalyzerTest.main()` to verify, then `BankStatementAnalyzer.main()` to analyze `statement.txt`.