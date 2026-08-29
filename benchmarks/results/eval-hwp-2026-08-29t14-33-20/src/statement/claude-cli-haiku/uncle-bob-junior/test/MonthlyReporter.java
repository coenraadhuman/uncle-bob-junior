import java.io.*;
import java.time.*;
import java.util.*;
import java.util.stream.*;

class MonthlyReporter {
    void report(List<Transaction> transactions) {
        groupByMonth(transactions).entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(this::printMonth);
    }
    
    private Map<String, List<Transaction>> groupByMonth(List<Transaction> transactions) {
        return transactions.stream()
            .collect(Collectors.groupingBy(this::monthKey));
    }
    
    private void printMonth(Map.Entry<String, List<Transaction>> entry) {
        System.out.println("\n=== " + entry.getKey() + " ===");
        printCategories(entry.getValue());
        printSuspicious(entry.getValue());
    }
    
    private void printCategories(List<Transaction> transactions) {
        var totals = transactions.stream()
            .collect(Collectors.groupingBy(
                Transaction::category,
                Collectors.summingDouble(Transaction::amountEur)
            ));
        totals.forEach((cat, total) -> 
            System.out.printf("%s: %.2f EUR\n", cat, total));
    }
    
    private void printSuspicious(List<Transaction> transactions) {
        var flagged = transactions.stream()
            .filter(Transaction::suspicious)
            .collect(Collectors.toList());
        
        if (flagged.isEmpty()) return;
        
        System.out.println("\nSuspicious transactions:");
        flagged.forEach(t -> 
            System.out.printf("  %s | %s | %.2f EUR\n", t.date(), t.description(), t.amountEur()));
    }
    
    private String monthKey(Transaction t) {
        return String.format("%d-%02d", t.date().getYear(), t.date().getMonthValue());
    }
}
