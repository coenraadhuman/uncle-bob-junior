import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.*;

class MonthlySummary {
    private final Map<YearMonth, Map<String, BigDecimal>> totals;

    MonthlySummary(List<TransactionInEur> enriched) {
        this.totals = buildTotals(enriched);
    }

    private Map<YearMonth, Map<String, BigDecimal>> buildTotals(List<TransactionInEur> enriched) {
        Map<YearMonth, Map<String, BigDecimal>> result = new TreeMap<>();
        
        for (TransactionInEur t : enriched) {
            YearMonth month = YearMonth.from(t.getDate());
            result.computeIfAbsent(month, k -> new HashMap<>())
                .merge(t.getCategory(), t.getAmount(), BigDecimal::add);
        }
        
        return result;
    }

    void printReport() {
        System.out.println("=== BANK STATEMENT ANALYSIS ===\n");
        
        for (Map.Entry<YearMonth, Map<String, BigDecimal>> entry : totals.entrySet()) {
            System.out.println("Month: " + entry.getKey());
            System.out.println("-".repeat(40));
            
            entry.getValue().entrySet().stream()
                .sorted((a, b) -> a.getKey().compareTo(b.getKey()))
                .forEach(cat -> System.out.printf("  %-12s: %10.2f EUR%n", cat.getKey(), cat.getValue()));
            
            System.out.println();
        }
    }
}
