import java.io.*;
import java.time.*;
import java.util.*;
import java.util.stream.*;

class StatementAnalyzer {
    private static final double SUSPICIOUS_AMOUNT_THRESHOLD = 2000.0;
    
    private final List<Transaction> transactions = new ArrayList<>();
    private final List<SuspiciousFlag> flagged = new ArrayList<>();
    
    void load(String filename) throws IOException {
        TransactionParser parser = new TransactionParser();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isBlank()) {
                    transactions.add(parser.parse(line));
                }
            }
        }
    }
    
    void analyze() {
        flagLargeTransactions();
        flagRepeatedAmountsPerDay();
    }
    
    private void flagLargeTransactions() {
        transactions.stream()
            .filter(t -> Math.abs(t.amountEur) > SUSPICIOUS_AMOUNT_THRESHOLD)
            .forEach(t -> flagged.add(
                new SuspiciousFlag(t, "Exceeds " + SUSPICIOUS_AMOUNT_THRESHOLD + " EUR")
            ));
    }
    
    private void flagRepeatedAmountsPerDay() {
        transactions.stream()
            .collect(Collectors.groupingBy(t -> t.date))
            .forEach((date, dayTxns) ->
                dayTxns.stream()
                    .collect(Collectors.groupingBy(t -> t.amountEur))
                    .forEach((amount, amountTxns) -> {
                        if (amountTxns.size() > 1) {
                            amountTxns.forEach(t -> flagged.add(
                                new SuspiciousFlag(t, 
                                    "Repeated amount " + amount + " EUR on " + date)
                            ));
                        }
                    })
            );
    }
    
    void printReport() {
        printMonthlySummary();
        printSuspiciousTransactions();
    }
    
    private void printMonthlySummary() {
        Map<YearMonth, Map<TransactionCategory, Double>> summary = 
            transactions.stream()
                .collect(Collectors.groupingBy(
                    t -> YearMonth.from(t.date),
                    Collectors.groupingBy(
                        t -> t.category,
                        Collectors.summingDouble(t -> t.amountEur)
                    )
                ));
        
        System.out.println("=== MONTHLY STATEMENT REPORT ===\n");
        
        summary.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> {
                YearMonth month = entry.getKey();
                Map<TransactionCategory, Double> categoryTotals = entry.getValue();
                
                System.out.println(month);
                for (TransactionCategory cat : TransactionCategory.values()) {
                    double total = categoryTotals.getOrDefault(cat, 0.0);
                    System.out.printf("  %-12s: %10.2f EUR\n", cat, total);
                }
                
                double monthSum = categoryTotals.values().stream()
                    .mapToDouble(Double::doubleValue)
                    .sum();
                System.out.printf("  %-12s: %10.2f EUR\n\n", "TOTAL", monthSum);
            });
    }
    
    private void printSuspiciousTransactions() {
        if (flagged.isEmpty()) {
            System.out.println("No suspicious transactions detected.");
            return;
        }
        
        System.out.println("=== SUSPICIOUS TRANSACTIONS ===\n");
        flagged.forEach(flag -> System.out.printf(
            "%s | %s | %.2f EUR | %s | %s\n",
            flag.transaction.date,
            flag.transaction.description,
            flag.transaction.amountEur,
            flag.transaction.category,
            flag.reason
        ));
    }
}
