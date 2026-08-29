I'll create a complete Java solution for bank statement analysis. Here are my assumptions:

- **Conversion rates:** USD = 0.92 EUR, GBP = 1.17 EUR
- **Category keywords:** salary (salary, wage, income), rent (rent, landlord, housing), groceries (Albert Heijn, supermarket, food)
- **Date format:** YYYY-MM-DD; month grouping extracts first 7 characters
- **Suspicious flags:** amounts > 2000 EUR or identical amounts appearing multiple times on the same day

```java
import java.io.*;
import java.time.YearMonth;
import java.util.*;

public class BankStatementAnalyzer {
    
    enum Category {
        SALARY, RENT, GROCERIES, OTHER
    }
    
    static class Transaction {
        String date;
        String description;
        double amountEur;
        Category category;
        boolean suspicious;
        
        Transaction(String date, String description, double amount, String currency) {
            this.date = date;
            this.description = description;
            this.amountEur = convertToEur(amount, currency);
            this.category = categorizeTransaction(description);
        }
    }
    
    private static double convertToEur(double amount, String currency) {
        return switch (currency.toUpperCase()) {
            case "EUR" -> amount;
            case "USD" -> amount * 0.92;
            case "GBP" -> amount * 1.17;
            default -> amount;
        };
    }
    
    private static Category categorizeTransaction(String description) {
        String desc = description.toLowerCase();
        
        if (desc.matches(".*\\b(salary|wage|payment|income|transfer in)\\b.*")) {
            return Category.SALARY;
        }
        if (desc.matches(".*\\b(rent|landlord|housing|apartment|mortgage)\\b.*")) {
            return Category.RENT;
        }
        if (desc.matches(".*\\b(albert heijn|supermarket|grocery|food|market|ah\\.nl)\\b.*")) {
            return Category.GROCERIES;
        }
        return Category.OTHER;
    }
    
    public static void main(String[] args) throws IOException {
        List<Transaction> transactions = readStatements("statement.txt");
        
        Map<YearMonth, Map<Category, Double>> monthlyTotals = new TreeMap<>();
        Map<YearMonth, List<Transaction>> transactionsByMonth = new TreeMap<>();
        
        for (Transaction t : transactions) {
            YearMonth month = YearMonth.parse(t.date.substring(0, 7));
            
            monthlyTotals.computeIfAbsent(month, k -> new EnumMap<>(Category.class))
                .merge(t.category, t.amountEur, Double::sum);
            
            transactionsByMonth.computeIfAbsent(month, k -> new ArrayList<>()).add(t);
        }
        
        flagSuspiciousTransactions(transactionsByMonth);
        printReport(monthlyTotals, transactionsByMonth);
    }
    
    private static List<Transaction> readStatements(String filename) throws IOException {
        List<Transaction> transactions = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                
                String[] parts = line.split(";");
                if (parts.length == 4) {
                    String date = parts[0].trim();
                    String description = parts[1].trim();
                    double amount = Double.parseDouble(parts[2].trim());
                    String currency = parts[3].trim();
                    transactions.add(new Transaction(date, description, amount, currency));
                }
            }
        }
        
        return transactions;
    }
    
    private static void flagSuspiciousTransactions(Map<YearMonth, List<Transaction>> byMonth) {
        for (List<Transaction> monthTransactions : byMonth.values()) {
            for (Transaction t : monthTransactions) {
                if (Math.abs(t.amountEur) > 2000) {
                    t.suspicious = true;
                }
            }
            
            Map<String, Map<Double, Integer>> sameDay = new HashMap<>();
            for (Transaction t : monthTransactions) {
                sameDay.computeIfAbsent(t.date, k -> new HashMap<>())
                    .merge(t.amountEur, 1, Integer::sum);
            }
            
            for (Transaction t : monthTransactions) {
                if (sameDay.get(t.date).get(t.amountEur) > 1) {
                    t.suspicious = true;
                }
            }
        }
    }
    
    private static void printReport(Map<YearMonth, Map<Category, Double>> monthlyTotals,
                                    Map<YearMonth, List<Transaction>> byMonth) {
        System.out.println("=== BANK STATEMENT ANALYSIS REPORT ===\n");
        
        for (YearMonth month : monthlyTotals.keySet()) {
            System.out.println("Month: " + month);
            System.out.println("-".repeat(50));
            
            Map<Category, Double> categories = monthlyTotals.get(month);
            for (Category cat : Category.values()) {
                double total = categories.getOrDefault(cat, 0.0);
                System.out.printf("  %-12s: %10.2f EUR\n", cat, total);
            }
            
            List<Transaction> suspicious = byMonth.get(month).stream()
                .filter(t -> t.suspicious)
                .toList();
            
            if (!suspicious.isEmpty()) {
                System.out.println("\n  SUSPICIOUS TRANSACTIONS:");
                for (Transaction t : suspicious) {
                    List<String> flags = new ArrayList<>();
                    if (Math.abs(t.amountEur) > 2000) {
                        flags.add("LARGE_AMOUNT");
                    }
                    long count = byMonth.get(month).stream()
                        .filter(x -> x.date.equals(t.date) && x.amountEur == t.amountEur)
                        .count();
                    if (count > 1) {
                        flags.add("DUPLICATE_SAME_DAY");
                    }
                    System.out.printf("    %s | %s | %.2f EUR | %s | [%s]\n",
                        t.date, t.description, t.amountEur, t.category, String.join(", ", flags));
                }
            }
            
            System.out.println();
        }
    }
}
```

The program reads `statement.txt`, parses each transaction, converts currencies, categorizes by keyword matching, groups by month, detects suspicious patterns, and outputs a detailed month-by-month report with category totals and flagged transactions.