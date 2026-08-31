import java.io.*;
import java.time.*;
import java.util.*;
import java.util.stream.*;

class MonthlyReport {
    private static final double SUSPICIOUS_THRESHOLD = 2000.0;
    
    private final YearMonth month;
    private final List<Transaction> transactions = new ArrayList<>();
    private final Map<String, Double> categoryTotals = new HashMap<>();
    
    MonthlyReport(YearMonth month) {
        this.month = month;
    }
    
    void addTransaction(Transaction t) {
        transactions.add(t);
        categoryTotals.merge(t.category(), t.amountEur(), Double::sum);
    }
    
    void print() {
        System.out.println("\n=== " + month + " ===");
        printCategoryTotals();
        printSuspiciousTransactions();
    }
    
    private void printCategoryTotals() {
        System.out.println("Category Totals:");
        categoryTotals.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(e -> System.out.printf("  %s: %.2f EUR%n", e.getKey(), e.getValue()));
    }
    
    private void printSuspiciousTransactions() {
        List<Transaction> suspicious = identifySuspicious();
        if (suspicious.isEmpty()) return;
        
        System.out.println("Suspicious Transactions:");
        for (Transaction t : suspicious) {
            System.out.printf("  %s | %s | %.2f EUR%n", t.date(), t.description(), t.amountEur());
        }
    }
    
    private List<Transaction> identifySuspicious() {
        Set<Transaction> suspicious = new HashSet<>();
        flagLargeTransactions(suspicious);
        flagDuplicateAmounts(suspicious);
        return suspicious.stream().sorted(Comparator.comparing(Transaction::date)).toList();
    }
    
    private void flagLargeTransactions(Set<Transaction> suspicious) {
        for (Transaction t : transactions) {
            if (Math.abs(t.amountEur()) > SUSPICIOUS_THRESHOLD) {
                suspicious.add(t);
            }
        }
    }
    
    private void flagDuplicateAmounts(Set<Transaction> suspicious) {
        Map<String, List<Transaction>> byDateAndAmount = new HashMap<>();
        for (Transaction t : transactions) {
            String key = t.date() + "|" + t.amountEur();
            byDateAndAmount.computeIfAbsent(key, k -> new ArrayList<>()).add(t);
        }
        
        for (List<Transaction> duplicates : byDateAndAmount.values()) {
            if (duplicates.size() > 1) {
                suspicious.addAll(duplicates);
            }
        }
    }
}
