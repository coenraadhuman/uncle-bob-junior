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
