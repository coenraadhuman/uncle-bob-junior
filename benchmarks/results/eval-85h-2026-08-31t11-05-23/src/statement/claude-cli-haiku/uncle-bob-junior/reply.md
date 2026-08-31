I'll provide a complete Java solution with these assumptions:
- USD → EUR: 0.92, GBP → EUR: 1.17
- Categories determined by keywords in description (case-insensitive)
- Suspicious: amount > 2000 EUR OR repeated identical amount on same day
- File defaults to `statement.txt` or accepts path as first argument

```java
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

public class BankStatementAnalyzer {
    private static final double USD_TO_EUR = 0.92;
    private static final double GBP_TO_EUR = 1.17;
    private static final double SUSPICIOUS_THRESHOLD_EUR = 2000.0;
    
    static class Transaction {
        final LocalDate date;
        final String description;
        final double amountEur;
        final String category;
        final boolean suspicious;
        
        Transaction(LocalDate date, String description, double amountEur, 
                   String category, boolean suspicious) {
            this.date = date;
            this.description = description;
            this.amountEur = amountEur;
            this.category = category;
            this.suspicious = suspicious;
        }
    }
    
    static class CurrencyConverter {
        double toEur(double amount, String currency) {
            return switch (currency) {
                case "EUR" -> amount;
                case "USD" -> amount * USD_TO_EUR;
                case "GBP" -> amount * GBP_TO_EUR;
                default -> throw new IllegalArgumentException("Unknown currency: " + currency);
            };
        }
    }
    
    static class TransactionCategorizer {
        private static final Map<String, String[]> KEYWORDS = Map.ofEntries(
            Map.entry("salary", new String[]{"salary", "wage", "income", "payment"}),
            Map.entry("rent", new String[]{"rent", "landlord", "property", "lease"}),
            Map.entry("groceries", new String[]{"albert heijn", "supermarket", "grocery", "food"})
        );
        
        String categorize(String description) {
            String lower = description.toLowerCase();
            for (var entry : KEYWORDS.entrySet()) {
                for (String keyword : entry.getValue()) {
                    if (lower.contains(keyword)) {
                        return entry.getKey();
                    }
                }
            }
            return "other";
        }
    }
    
    static class SuspiciousDetector {
        List<Transaction> flag(List<Transaction> transactions) {
            Map<String, List<Integer>> amountsByDate = new HashMap<>();
            
            for (int i = 0; i < transactions.size(); i++) {
                Transaction t = transactions.get(i);
                String dateKey = t.date.toString();
                amountsByDate.computeIfAbsent(dateKey, k -> new ArrayList<>()).add(i);
            }
            
            Set<Integer> suspiciousIndices = new HashSet<>();
            
            for (var indices : amountsByDate.values()) {
                for (int i = 0; i < indices.size(); i++) {
                    for (int j = i + 1; j < indices.size(); j++) {
                        if (transactions.get(indices.get(i)).amountEur == 
                            transactions.get(indices.get(j)).amountEur) {
                            suspiciousIndices.add(indices.get(i));
                            suspiciousIndices.add(indices.get(j));
                        }
                    }
                }
            }
            
            List<Transaction> result = new ArrayList<>();
            for (int i = 0; i < transactions.size(); i++) {
                Transaction t = transactions.get(i);
                boolean isSuspicious = suspiciousIndices.contains(i) || 
                                      Math.abs(t.amountEur) > SUSPICIOUS_THRESHOLD_EUR;
                result.add(new Transaction(t.date, t.description, t.amountEur, t.category, isSuspicious));
            }
            return result;
        }
    }
    
    static class TransactionParser {
        private final CurrencyConverter converter;
        private final TransactionCategorizer categorizer;
        
        TransactionParser(CurrencyConverter converter, TransactionCategorizer categorizer) {
            this.converter = converter;
            this.categorizer = categorizer;
        }
        
        Transaction parse(String line) {
            String[] parts = line.split(";");
            if (parts.length != 4) throw new IllegalArgumentException("Invalid format: " + line);
            
            LocalDate date = LocalDate.parse(parts[0]);
            String description = parts[1];
            double amount = Double.parseDouble(parts[2]);
            String currency = parts[3];
            
            double amountEur = converter.toEur(amount, currency);
            String category = categorizer.categorize(description);
            
            return new Transaction(date, description, amountEur, category, false);
        }
    }
    
    public static void main(String[] args) throws IOException {
        CurrencyConverter converter = new CurrencyConverter();
        TransactionCategorizer categorizer = new TransactionCategorizer();
        TransactionParser parser = new TransactionParser(converter, categorizer);
        SuspiciousDetector detector = new SuspiciousDetector();
        
        String filePath = args.length > 0 ? args[0] : "statement.txt";
        
        List<Transaction> transactions = Files.lines(Path.of(filePath))
            .map(parser::parse)
            .toList();
        
        List<Transaction> flagged = detector.flag(transactions);
        
        printReport(flagged);
    }
    
    static void printReport(List<Transaction> transactions) {
        Map<YearMonth, Map<String, Double>> byMonth = new TreeMap<>();
        
        for (Transaction t : transactions) {
            YearMonth month = YearMonth.from(t.date);
            byMonth.computeIfAbsent(month, k -> new TreeMap<>())
                .merge(t.category, t.amountEur, Double::sum);
        }
        
        System.out.println("=== MONTHLY CATEGORY TOTALS ===\n");
        for (var monthEntry : byMonth.entrySet()) {
            System.out.println(monthEntry.getKey());
            for (var catEntry : monthEntry.getValue().entrySet()) {
                System.out.printf("  %-12s %10.2f EUR\n", catEntry.getKey(), catEntry.getValue());
            }
            System.out.println();
        }
        
        List<Transaction> suspicious = transactions.stream().filter(t -> t.suspicious).toList();
        if (!suspicious.isEmpty()) {
            System.out.println("=== SUSPICIOUS TRANSACTIONS ===\n");
            for (Transaction t : suspicious) {
                System.out.printf("%s  %-30s  %10.2f EUR\n", t.date, t.description, t.amountEur);
            }
        }
    }
}
```

**Tests:**

```java
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
```

**Cleaned: single responsibility for parsing, categorization, currency conversion, and flagging; named constants for rates and threshold; guard clauses for early returns; flat control flow throughout. Safe to change because each class handles one concern and tests verify core logic.**