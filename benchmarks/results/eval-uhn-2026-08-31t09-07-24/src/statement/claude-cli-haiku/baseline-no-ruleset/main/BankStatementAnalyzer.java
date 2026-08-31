import java.io.*;
import java.time.*;
import java.util.*;

public class BankStatementAnalyzer {
    private static final double USD_TO_EUR = 0.92;
    private static final double GBP_TO_EUR = 1.17;
    
    enum Category {
        SALARY, RENT, GROCERIES, OTHER
    }
    
    static class Transaction {
        LocalDate date;
        String description;
        double amountEUR;
        String originalCurrency;
        Category category;
        boolean suspicious;
        
        Transaction(String line) {
            String[] parts = line.split(";");
            this.date = LocalDate.parse(parts[0]);
            this.description = parts[1];
            double amount = Double.parseDouble(parts[2]);
            String currency = parts[3].trim();
            this.originalCurrency = currency;
            
            this.amountEUR = convertToEUR(amount, currency);
            this.category = categorize(description);
            this.suspicious = false;
        }
        
        private double convertToEUR(double amount, String currency) {
            return switch(currency) {
                case "EUR" -> amount;
                case "USD" -> amount * USD_TO_EUR;
                case "GBP" -> amount * GBP_TO_EUR;
                default -> amount;
            };
        }
        
        private Category categorize(String desc) {
            String upper = desc.toUpperCase();
            if (upper.matches(".*(SALARY|WAGES|PAYROLL|PAYMENT FROM).*")) {
                return Category.SALARY;
            }
            if (upper.matches(".*(RENT|LANDLORD|HOUSING).*")) {
                return Category.RENT;
            }
            if (upper.matches(".*(ALBERT HEIJN|SUPERMARKET|GROCERY|MARKET|FOOD|AH).*")) {
                return Category.GROCERIES;
            }
            return Category.OTHER;
        }
    }
    
    public static void main(String[] args) throws IOException {
        List<Transaction> transactions = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader("statement.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    transactions.add(new Transaction(line));
                }
            }
        }
        
        detectSuspicious(transactions);
        
        Map<YearMonth, Map<Category, Double>> monthlyTotals = new TreeMap<>();
        Map<YearMonth, List<String>> suspiciousFlags = new TreeMap<>();
        
        for (Transaction t : transactions) {
            YearMonth month = YearMonth.from(t.date);
            
            monthlyTotals.computeIfAbsent(month, k -> new EnumMap<>(Category.class))
                .merge(t.category, t.amountEUR, Double::sum);
            
            if (t.suspicious) {
                String flag = String.format("%s | %s | %.2f EUR", 
                    t.date, t.description, t.amountEUR);
                suspiciousFlags.computeIfAbsent(month, k -> new ArrayList<>()).add(flag);
            }
        }
        
        System.out.println("=== BANK STATEMENT ANALYSIS ===\n");
        
        for (YearMonth month : monthlyTotals.keySet()) {
            System.out.println(month + " REPORT");
            System.out.println("-".repeat(50));
            
            Map<Category, Double> totals = monthlyTotals.get(month);
            for (Category cat : Category.values()) {
                double total = totals.getOrDefault(cat, 0.0);
                System.out.printf("%-12s: %10.2f EUR\n", cat, total);
            }
            
            double monthTotal = totals.values().stream().mapToDouble(Double::doubleValue).sum();
            System.out.printf("%-12s: %10.2f EUR\n", "TOTAL", monthTotal);
            
            if (suspiciousFlags.containsKey(month)) {
                System.out.println("\nSuspicious Transactions:");
                for (String flag : suspiciousFlags.get(month)) {
                    System.out.println("  ⚠ " + flag);
                }
            }
            
            System.out.println();
        }
    }
    
    private static void detectSuspicious(List<Transaction> transactions) {
        for (Transaction t : transactions) {
            if (Math.abs(t.amountEUR) > 2000) {
                t.suspicious = true;
            }
        }
        
        Map<String, List<Transaction>> sameDay = new HashMap<>();
        for (Transaction t : transactions) {
            String key = t.date + ":" + String.format("%.2f", t.amountEUR);
            sameDay.computeIfAbsent(key, k -> new ArrayList<>()).add(t);
        }
        
        for (List<Transaction> group : sameDay.values()) {
            if (group.size() > 1) {
                for (Transaction t : group) {
                    t.suspicious = true;
                }
            }
        }
    }
}
