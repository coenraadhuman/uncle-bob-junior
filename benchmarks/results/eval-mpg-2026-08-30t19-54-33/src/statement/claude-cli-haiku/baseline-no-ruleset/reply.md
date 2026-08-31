I'll create a complete Java solution for analyzing bank statements. Here are my assumptions:

**Assumptions:**
- Date format: YYYY-MM-DD (ISO)
- Fixed conversion rates: 1 USD = 0.95 EUR, 1 GBP = 1.17 EUR
- Category keywords (case-insensitive): salary includes "salary/wage/income"; rent includes "rent/landlord"; groceries includes "albert heijn/supermarket/grocery/groceries"
- Repeated identical amounts = exact EUR amount match on same day
- Suspicious flag applies to absolute value

```java
import java.io.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

public class BankStatementAnalyzer {
    private static final double USD_TO_EUR = 0.95;
    private static final double GBP_TO_EUR = 1.17;
    
    static class Transaction {
        LocalDate date;
        String description;
        double amountEur;
        String category;
        boolean isSuspicious;
        
        Transaction(LocalDate date, String description, double amountEur, String category) {
            this.date = date;
            this.description = description;
            this.amountEur = amountEur;
            this.category = category;
        }
    }
    
    public static void main(String[] args) {
        List<Transaction> transactions = readStatements("statement.txt");
        flagSuspiciousTransactions(transactions);
        printMonthlyReport(transactions);
    }
    
    private static List<Transaction> readStatements(String filename) {
        List<Transaction> transactions = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length == 4) {
                    LocalDate date = LocalDate.parse(parts[0].trim());
                    String description = parts[1].trim();
                    double amount = Double.parseDouble(parts[2].trim());
                    String currency = parts[3].trim();
                    
                    double amountEur = convertToEur(amount, currency);
                    String category = categorizeTransaction(description);
                    transactions.add(new Transaction(date, description, amountEur, category));
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
        return transactions;
    }
    
    private static double convertToEur(double amount, String currency) {
        return switch (currency.toUpperCase()) {
            case "USD" -> amount * USD_TO_EUR;
            case "GBP" -> amount * GBP_TO_EUR;
            default -> amount;
        };
    }
    
    private static String categorizeTransaction(String description) {
        String desc = description.toLowerCase();
        if (desc.matches(".*\\b(salary|wage|income)\\b.*")) return "salary";
        if (desc.matches(".*\\b(rent|landlord)\\b.*")) return "rent";
        if (desc.matches(".*\\b(albert heijn|supermarket|grocery|groceries)\\b.*")) return "groceries";
        return "other";
    }
    
    private static void flagSuspiciousTransactions(List<Transaction> transactions) {
        for (Transaction t : transactions) {
            if (Math.abs(t.amountEur) > 2000) {
                t.isSuspicious = true;
            }
        }
        
        Map<LocalDate, Map<Double, Integer>> dateAmounts = new HashMap<>();
        for (Transaction t : transactions) {
            dateAmounts.computeIfAbsent(t.date, k -> new HashMap<>())
                      .merge(t.amountEur, 1, Integer::sum);
        }
        
        for (Transaction t : transactions) {
            if (dateAmounts.get(t.date).getOrDefault(t.amountEur, 0) > 1) {
                t.isSuspicious = true;
            }
        }
    }
    
    private static void printMonthlyReport(List<Transaction> transactions) {
        Map<YearMonth, Map<String, Double>> monthlyTotals = new TreeMap<>();
        Map<YearMonth, List<Transaction>> monthlyTransactions = new TreeMap<>();
        
        for (Transaction t : transactions) {
            YearMonth month = YearMonth.from(t.date);
            monthlyTotals.computeIfAbsent(month, k -> new HashMap<>())
                        .merge(t.category, t.amountEur, Double::sum);
            monthlyTransactions.computeIfAbsent(month, k -> new ArrayList<>()).add(t);
        }
        
        for (YearMonth month : monthlyTotals.keySet()) {
            System.out.println("\n=== " + month + " ===");
            
            Map<String, Double> totals = monthlyTotals.get(month);
            for (String category : Arrays.asList("salary", "rent", "groceries", "other")) {
                double total = totals.getOrDefault(category, 0.0);
                System.out.printf("%s: %.2f EUR%n", category, total);
            }
            
            List<Transaction> suspicious = monthlyTransactions.get(month).stream()
                                                              .filter(t -> t.isSuspicious)
                                                              .toList();
            if (!suspicious.isEmpty()) {
                System.out.println("  Suspicious transactions:");
                for (Transaction t : suspicious) {
                    System.out.printf("    %s - %s - %.2f EUR%n", t.date, t.description, t.amountEur);
                }
            }
        }
    }
}
```

The solution reads statement.txt, converts currencies, categorizes by keywords, flags transactions over 2000 EUR or repeated amounts on the same day, and outputs a monthly summary with categories and suspicious items highlighted.