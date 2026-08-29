import java.io.*;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

public class BankStatementAnalyzer {
    
    private static final double USD_TO_EUR = 0.92;
    private static final double GBP_TO_EUR = 1.17;
    
    static class Transaction {
        LocalDate date;
        String description;
        double amountEUR;
        String category;
        boolean suspicious;
        
        Transaction(LocalDate date, String description, double amount, String currency) {
            this.date = date;
            this.description = description;
            this.amountEUR = convertToEUR(amount, currency);
            this.category = categorize(description);
            this.suspicious = false;
        }
        
        private double convertToEUR(double amount, String currency) {
            return switch (currency.toUpperCase()) {
                case "USD" -> amount * USD_TO_EUR;
                case "GBP" -> amount * GBP_TO_EUR;
                default -> amount;
            };
        }
        
        private String categorize(String desc) {
            String lower = desc.toLowerCase();
            if (lower.matches(".*(salary|wages|income|paycheck|bonus).*")) return "salary";
            if (lower.matches(".*(rent|landlord|apartment|housing).*")) return "rent";
            if (lower.matches(".*(groceries|supermarket|albert heijn|tesco|marks|carrefour|ah).*")) return "groceries";
            return "other";
        }
    }
    
    public static void main(String[] args) throws IOException {
        List<Transaction> transactions = readStatements("statement.txt");
        flagSuspicious(transactions);
        
        Map<YearMonth, Map<String, Double>> monthlyTotals = groupByMonthAndCategory(transactions);
        Map<YearMonth, List<Transaction>> suspiciousByMonth = groupSuspiciousByMonth(transactions);
        
        printReport(monthlyTotals, suspiciousByMonth);
    }
    
    private static List<Transaction> readStatements(String filename) throws IOException {
        List<Transaction> transactions = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length == 4) {
                    LocalDate date = LocalDate.parse(parts[0]);
                    String description = parts[1];
                    double amount = Double.parseDouble(parts[2]);
                    String currency = parts[3];
                    transactions.add(new Transaction(date, description, amount, currency));
                }
            }
        }
        return transactions;
    }
    
    private static void flagSuspicious(List<Transaction> transactions) {
        for (Transaction t : transactions) {
            if (Math.abs(t.amountEUR) > 2000) {
                t.suspicious = true;
            }
        }
        
        Map<LocalDate, Map<Double, Integer>> counts = new HashMap<>();
        for (Transaction t : transactions) {
            double absAmount = Math.abs(t.amountEUR);
            counts.computeIfAbsent(t.date, k -> new HashMap<>())
                .merge(absAmount, 1, Integer::sum);
        }
        
        for (Transaction t : transactions) {
            if (counts.get(t.date).get(Math.abs(t.amountEUR)) > 1) {
                t.suspicious = true;
            }
        }
    }
    
    private static Map<YearMonth, Map<String, Double>> groupByMonthAndCategory(List<Transaction> transactions) {
        Map<YearMonth, Map<String, Double>> result = new TreeMap<>();
        
        for (Transaction t : transactions) {
            YearMonth month = YearMonth.from(t.date);
            Map<String, Double> categories = result.computeIfAbsent(month, k -> new HashMap<>());
            categories.put(t.category, categories.getOrDefault(t.category, 0.0) + t.amountEUR);
        }
        
        return result;
    }
    
    private static Map<YearMonth, List<Transaction>> groupSuspiciousByMonth(List<Transaction> transactions) {
        return transactions.stream()
            .filter(t -> t.suspicious)
            .collect(Collectors.groupingBy(t -> YearMonth.from(t.date), TreeMap::new, Collectors.toList()));
    }
    
    private static void printReport(Map<YearMonth, Map<String, Double>> monthlyTotals, Map<YearMonth, List<Transaction>> suspiciousByMonth) {
        System.out.println("=== BANK STATEMENT ANALYSIS ===\n");
        
        for (Map.Entry<YearMonth, Map<String, Double>> entry : monthlyTotals.entrySet()) {
            YearMonth month = entry.getKey();
            Map<String, Double> categories = entry.getValue();
            
            System.out.println("Month: " + month);
            System.out.println("---");
            
            double total = 0;
            for (String category : Arrays.asList("salary", "rent", "groceries", "other")) {
                double amount = categories.getOrDefault(category, 0.0);
                System.out.printf("  %-12s: %10.2f EUR%n", category, amount);
                total += amount;
            }
            
            System.out.printf("  %-12s: %10.2f EUR%n", "TOTAL", total);
            
            if (suspiciousByMonth.containsKey(month)) {
                System.out.println("\n  Suspicious Transactions:");
                for (Transaction t : suspiciousByMonth.get(month)) {
                    String flag = Math.abs(t.amountEUR) > 2000 ? "[>2000 EUR]" : "[Duplicate amount]";
                    System.out.printf("    %s | %-35s | %10.2f EUR %s%n", t.date, t.description, t.amountEUR, flag);
                }
            }
            
            System.out.println();
        }
    }
}
