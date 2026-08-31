import java.io.*;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

class BankStatementAnalyser {
    private final TransactionParser parser;
    private final TransactionEnricher enricher;
    private final SuspiciousDetector detector;
    
    BankStatementAnalyser() {
        parser = new TransactionParser();
        enricher = new TransactionEnricher(new Categorizer());
        detector = new SuspiciousDetector();
    }
    
    void analyse(String filename) throws IOException {
        List<Transaction> transactions = readTransactions(filename);
        Map<YearMonth, MonthlyReport> reports = buildReports(transactions);
        printReports(reports);
    }
    
    private List<Transaction> readTransactions(String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            return reader.lines()
                .map(parser::parse)
                .map(enricher::enrich)
                .collect(Collectors.toList());
        }
    }
    
    private Map<YearMonth, MonthlyReport> buildReports(List<Transaction> transactions) {
        return transactions.stream()
            .collect(Collectors.groupingBy(t -> YearMonth.from(t.date())))
            .entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> buildMonthReport(e.getKey(), e.getValue(), transactions)
            ));
    }
    
    private MonthlyReport buildMonthReport(YearMonth month, List<Transaction> monthTransactions, 
                                          List<Transaction> allTransactions) {
        Map<Category, BigDecimal> totals = sumByCategory(monthTransactions);
        List<Transaction> flagged = findFlagged(monthTransactions);
        return new MonthlyReport(month, totals, flagged);
    }
    
    private Map<Category, BigDecimal> sumByCategory(List<Transaction> transactions) {
        Map<Category, BigDecimal> map = new EnumMap<>(Category.class);
        for (Category cat : Category.values()) {
            map.put(cat, BigDecimal.ZERO);
        }
        for (Transaction tx : transactions) {
            map.merge(tx.category(), tx.amountEur(), BigDecimal::add);
        }
        return map;
    }
    
    private List<Transaction> findFlagged(List<Transaction> transactions) {
        List<Transaction> flagged = new ArrayList<>();
        for (Transaction tx : transactions) {
            if (detector.isLarge(tx)) {
                flagged.add(tx);
            } else if (detector.hasDuplicateOnDay(tx, transactions)) {
                flagged.add(tx);
            }
        }
        return flagged;
    }
    
    private void printReports(Map<YearMonth, MonthlyReport> reports) {
        List<YearMonth> months = new ArrayList<>(reports.keySet());
        Collections.sort(months);
        for (YearMonth month : months) {
            printMonth(reports.get(month));
        }
    }
    
    private void printMonth(MonthlyReport report) {
        System.out.println("\n=== " + report.month() + " ===");
        System.out.println("Category Totals:");
        for (Category cat : Category.values()) {
            BigDecimal total = report.totals().get(cat);
            System.out.printf("  %-10s: %10.2f EUR%n", cat, total);
        }
        printFlagged(report.flaggedTransactions());
    }
    
    private void printFlagged(List<Transaction> flagged) {
        if (flagged.isEmpty()) {
            System.out.println("No suspicious transactions.");
            return;
        }
        System.out.println("Suspicious Transactions:");
        for (Transaction tx : flagged) {
            System.out.printf("  %s | %-30s | %10.2f EUR%n", 
                tx.date(), tx.description(), tx.amountEur());
        }
    }
}
