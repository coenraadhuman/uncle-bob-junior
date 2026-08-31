```java
import java.io.*;
import java.util.*;
import java.time.LocalDate;
import java.time.YearMonth;

public class BankStatementAnalyzer {
    private static final double USD_TO_EUR = 0.92;
    private static final double GBP_TO_EUR = 1.17;
    
    static class Transaction {
        LocalDate date;
        String description;
        double amountEUR;
        String category;
        boolean suspicious;
        
        Transaction(LocalDate date, String description, double amountEUR, String category) {
            this.date = date;
            this.description = description;
            this.amountEUR = amountEUR;
            this.category = category;
            this.suspicious = false;
        }
    }
    
    public static void main(String[] args) throws IOException {
        List<Transaction> transactions = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader("statement.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                
                String[] parts = line.split(";");
                if (parts.length != 4) continue;
                
                LocalDate date = LocalDate.parse(parts[0].trim());
                String description = parts[1].trim();
                double amount = Double.parseDouble(parts[2].trim());
                String currency = parts[3].trim();
                
                double amountEUR = convertToEUR(amount, currency);
                String category = categorize(description);
                
                transactions.add(new Transaction(date, description, amountEUR, category));
            }
        }
        
        flagSuspicious(transactions);
        
        Map<YearMonth, Map<String, Double>> monthlyTotals = new TreeMap<>();
        Map<YearMonth, List<String>> suspiciousFlags = new TreeMap<>();
        
        for (Transaction t : transactions) {
            YearMonth month = YearMonth.from(t.date);
            
            monthlyTotals.putIfAbsent(month, new HashMap<>());
            monthlyTotals.get(month).put(t.category, 
                monthlyTotals.get(month).getOrDefault(t.category, 0.0) + t.amountEUR);
            
            if (t.suspicious) {
                suspiciousFlags.putIfAbsent(month, new ArrayList<>());
                String reason = t.amountEUR > 2000 ? "Amount > 2000 EUR" : "Duplicate amount on same day";
                suspiciousFlags.get(month).add(
                    String.format("%s: %s (%.2f EUR) - %s", t.date, t.description, t.amountEUR, reason)
                );
            }
        }
        
        for (YearMonth month : monthlyTotals.keySet()) {
            System.out.println("\n=== " + month + " ===");
            Map<String, Double> totals = monthlyTotals.get(month);
            
            double monthTotal = 0;
            for (String category : Arrays.asList("salary", "rent", "groceries", "other")) {
                double amount = totals.getOrDefault(category, 0.0);
                System.out.printf("%s: %.2f EUR%n", category, amount);
                monthTotal += amount;
            }
            System.out.printf("Total: %.2f EUR%n", monthTotal);
            
            if (suspiciousFlags.containsKey(month)) {
                System.out.println("\nSuspicious transactions:");
                for (String flag : suspiciousFlags.get(month)) {
                    System.out.println("  - " + flag);
                }
            }
        }
    }
    
    private static double convertToEUR(double amount, String currency) {
        return switch (currency.toUpperCase()) {
            case "USD" -> amount * USD_TO_EUR;
            case "GBP" -> amount * GBP_TO_EUR;
            case "EUR" -> amount;
            default -> amount;
        };
    }
    
    private static String categorize(String description) {
        String desc = description.toLowerCase();
        
        if (desc.contains("salary") || desc.contains("wage") || 
            desc.contains("income")) {
            return "salary";
        }
        if (desc.contains("rent") || desc.contains("landlord") || 
            desc.contains("mortgage")) {
            return "rent";
        }
        if (desc.contains("albert heijn") || desc.contains("supermarket") || 
            desc.contains("grocery") || desc.contains("food") || desc.contains("market")) {
            return "groceries";
        }
        return "other";
    }
    
    private static void flagSuspicious(List<Transaction> transactions) {
        for (Transaction t : transactions) {
            if (t.amountEUR > 2000) {
                t.suspicious = true;
            }
        }
        
        Map<String, Integer> dayAmountCount = new HashMap<>();
        for (Transaction t : transactions) {
            String key = t.date + ":" + String.format("%.2f", t.amountEUR);
            dayAmountCount.put(key, dayAmountCount.getOrDefault(key, 0) + 1);
        }
        
        for (Transaction t : transactions) {
            String key = t.date + ":" + String.format("%.2f", t.amountEUR);
            if (dayAmountCount.get(key) > 1) {
                t.suspicious = true;
            }
        }
    }
}
```

**Assumptions:**
- Fixed conversion rates: USD = 0.92 EUR, GBP = 1.17 EUR
- Keywords: "salary/wage/income" → salary; "rent/landlord/mortgage" → rent; "albert heijn/supermarket/grocery/food/market" → groceries; others → other
- Suspicious criteria: single transaction >2000 EUR absolute value, or 2+ identical amounts on the same day
- Date format: YYYY-MM-DD, amounts as decimals
- Requires Java 12+ for switch expressions