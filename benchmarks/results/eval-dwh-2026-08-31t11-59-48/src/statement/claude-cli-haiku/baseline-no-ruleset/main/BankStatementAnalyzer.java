import java.io.*;
import java.time.*;
import java.util.*;
import java.util.stream.*;

public class BankStatementAnalyzer {
    private static final double USD_TO_EUR = 0.92;
    private static final double GBP_TO_EUR = 1.17;
    private static final List<String> CATEGORY_ORDER = Arrays.asList("salary", "rent", "groceries", "other");
    
    static class Transaction {
        LocalDate date;
        String description;
        double originalAmount;
        double amountEUR;
        String originalCurrency;
        String category;
        boolean suspicious;
        
        Transaction(LocalDate date, String description, double amount, String currency) {
            this.date = date;
            this.description = description;
            this.originalAmount = amount;
            this.originalCurrency = currency;
            this.amountEUR = convertToEUR(amount, currency);
            this.category = categorize(description);
            this.suspicious = false;
        }
        
        private double convertToEUR(double amount, String currency) {
            return switch (currency.toUpperCase()) {
                case "USD" -> amount * USD_TO_EUR;
                case "GBP" -> amount * GBP_TO_EUR;
                case "EUR" -> amount;
                default -> amount;
            };
        }
        
        private String categorize(String desc) {
            String lower = desc.toLowerCase();
            if (lower.contains("salary") || lower.contains("wage") || lower.contains("income") || lower.contains("payment")) {
                return "salary";
            }
            if (lower.contains("rent") || lower.contains("mortgage") || lower.contains("landlord")) {
                return "rent";
            }
            if (lower.contains("albert heijn") || lower.contains("supermarket") || lower.contains("grocery") || lower.contains("food") || lower.contains("market")) {
                return "groceries";
            }
            return "other";
        }
    }
    
    public static void main(String[] args) throws IOException {
        List<Transaction> transactions = readTransactions("statement.txt");
        flagSuspiciousTransactions(transactions);
        
        Map<YearMonth, Map<String, Double>> monthlyCategoryTotals = 
            transactions.stream()
                .collect(Collectors.groupingBy(
                    t -> YearMonth.from(t.date),
                    Collectors.groupingBy(
                        t -> t.category,
                        Collectors.summingDouble(t -> t.amountEUR)
                    )
                ));
        
        printReport(transactions, monthlyCategoryTotals);
    }
    
    private static List<Transaction> readTransactions(String filename) throws IOException {
        List<Transaction> transactions = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(";");
                if (parts.length == 4) {
                    LocalDate date = LocalDate.parse(parts[0].trim());
                    String description = parts[1].trim();
                    double amount = Double.parseDouble(parts[2].trim());
                    String currency = parts[3].trim();
                    transactions.add(new Transaction(date, description, amount, currency));
                }
            }
        }
        return transactions;
    }
    
    private static void flagSuspiciousTransactions(List<Transaction> transactions) {
        transactions.forEach(t -> {
            if (Math.abs(t.amountEUR) > 2000) {
                t.suspicious = true;
            }
        });
        
        Map<String, List<Transaction>> byDateAndAmount = 
            transactions.stream()
                .collect(Collectors.groupingBy(t -> t.date + "|" + t.amountEUR));
        
        byDateAndAmount.forEach((key, txns) -> {
            if (txns.size() > 1) {
                txns.forEach(t -> t.suspicious = true);
            }
        });
    }
    
    private static void printReport(List<Transaction> transactions, 
                                   Map<YearMonth, Map<String, Double>> monthlyCategoryTotals) {
        System.out.println("=== BANK STATEMENT ANALYSIS REPORT ===\n");
        
        List<YearMonth> months = monthlyCategoryTotals.keySet().stream()
            .sorted()
            .collect(Collectors.toList());
        
        for (YearMonth month : months) {
            System.out.println("Month: " + month);
            System.out.println("----------------------------------------");
            
            Map<String, Double> categories = monthlyCategoryTotals.get(month);
            double monthTotal = 0;
            
            for (String cat : CATEGORY_ORDER) {
                double total = categories.getOrDefault(cat, 0.0);
                System.out.printf("  %-12s: %10.2f EUR%n", cat, total);
                monthTotal += total;
            }
            
            System.out.printf("  %-12s: %10.2f EUR%n", "TOTAL", monthTotal);
            System.out.println();
            System.out.println("Suspicious transactions:");
            
            List<Transaction> suspiciousInMonth = transactions.stream()
                .filter(t -> YearMonth.from(t.date).equals(month) && t.suspicious)
                .collect(Collectors.toList());
            
            if (suspiciousInMonth.isEmpty()) {
                System.out.println("  None");
            } else {
                suspiciousInMonth.forEach(t -> 
                    System.out.printf("  [!] %s | %s | %.2f EUR (orig: %.2f %s)%n", 
                        t.date, t.description, t.amountEUR, t.originalAmount, t.originalCurrency)
                );
            }
            
            System.out.println();
        }
        
        System.out.println("=== END OF REPORT ===");
    }
}
