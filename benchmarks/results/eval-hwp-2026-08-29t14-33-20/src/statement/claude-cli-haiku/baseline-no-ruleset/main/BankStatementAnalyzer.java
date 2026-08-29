import java.io.*;
import java.time.*;
import java.util.*;
import java.util.stream.*;

// Assumptions:
// - USD to EUR: 0.92, GBP to EUR: 1.17 (fixed rates)
// - Categories: salary/wage/income, rent/mortgage, Albert Heijn/supermarket/grocery/food, else other
// - Suspicious: amounts > 2000 EUR or same EUR amount repeated on same day

public class BankStatementAnalyzer {
    
    static class Transaction {
        LocalDate date;
        String description;
        double amount;
        String currency;
        double amountEUR;
        String category;
        boolean suspicious;
        
        Transaction(LocalDate date, String description, double amount, String currency) {
            this.date = date;
            this.description = description;
            this.amount = amount;
            this.currency = currency;
            this.amountEUR = convertToEUR(amount, currency);
            this.category = categorize(description);
            this.suspicious = false;
        }
    }
    
    static double convertToEUR(double amount, String currency) {
        return switch (currency) {
            case "USD" -> amount * 0.92;
            case "GBP" -> amount * 1.17;
            case "EUR" -> amount;
            default -> amount;
        };
    }
    
    static String categorize(String description) {
        String desc = description.toLowerCase();
        if (desc.contains("salary") || desc.contains("wage") || desc.contains("income")) {
            return "salary";
        }
        if (desc.contains("rent") || desc.contains("mortgage")) {
            return "rent";
        }
        if (desc.contains("albert heijn") || desc.contains("supermarket") || 
            desc.contains("grocery") || desc.contains("food")) {
            return "groceries";
        }
        return "other";
    }
    
    public static void main(String[] args) {
        List<Transaction> transactions = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader("statement.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length == 4) {
                    LocalDate date = LocalDate.parse(parts[0]);
                    String description = parts[1];
                    double amount = Double.parseDouble(parts[2]);
                    String currency = parts[3].toUpperCase();
                    transactions.add(new Transaction(date, description, amount, currency));
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            return;
        }
        
        flagSuspiciousTransactions(transactions);
        
        Map<YearMonth, Map<String, List<Transaction>>> monthlyData = 
            transactions.stream().collect(Collectors.groupingBy(
                t -> YearMonth.from(t.date),
                Collectors.groupingBy(t -> t.category)
            ));
        
        monthlyData.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> printMonthlyReport(entry.getKey(), entry.getValue()));
    }
    
    static void flagSuspiciousTransactions(List<Transaction> transactions) {
        for (Transaction t : transactions) {
            if (Math.abs(t.amountEUR) > 2000) {
                t.suspicious = true;
            }
        }
        
        Map<LocalDate, Map<Double, Long>> amountCounts = transactions.stream()
            .collect(Collectors.groupingBy(
                t -> t.date,
                Collectors.groupingBy(t -> t.amountEUR, Collectors.counting())
            ));
        
        for (Transaction t : transactions) {
            if (amountCounts.get(t.date).getOrDefault(t.amountEUR, 0L) > 1) {
                t.suspicious = true;
            }
        }
    }
    
    static void printMonthlyReport(YearMonth month, Map<String, List<Transaction>> categories) {
        System.out.println("\n=== " + month + " ===");
        
        double grandTotal = 0;
        String[] categoryOrder = {"salary", "rent", "groceries", "other"};
        
        for (String category : categoryOrder) {
            List<Transaction> catTransactions = categories.getOrDefault(category, new ArrayList<>());
            double total = catTransactions.stream().mapToDouble(t -> t.amountEUR).sum();
            grandTotal += total;
            
            System.out.printf("%-12s: %10.2f EUR (%d transactions)\n", category, total, catTransactions.size());
            
            for (Transaction t : catTransactions) {
                String flag = t.suspicious ? " [SUSPICIOUS]" : "";
                System.out.printf("  %s | %-30s | %10.2f EUR%s\n", 
                    t.date, t.description, t.amountEUR, flag);
            }
        }
        
        System.out.printf("%-12s: %10.2f EUR\n", "TOTAL", grandTotal);
    }
}
