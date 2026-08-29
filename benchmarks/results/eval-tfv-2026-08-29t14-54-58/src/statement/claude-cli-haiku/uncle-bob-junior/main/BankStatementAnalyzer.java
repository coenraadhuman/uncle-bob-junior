import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

public class BankStatementAnalyzer {
    
    enum Currency {
        EUR(BigDecimal.ONE),
        USD(BigDecimal.valueOf(1.10)),
        GBP(BigDecimal.valueOf(1.17));
        
        final BigDecimal toEur;
        
        Currency(BigDecimal toEur) {
            this.toEur = toEur;
        }
    }
    
    enum Category {
        SALARY, RENT, GROCERIES, OTHER
    }
    
    record Transaction(
        LocalDate date,
        String description,
        BigDecimal amountEur,
        Category category,
        boolean suspicious
    ) {}
    
    static class StatementParser {
        private static final String SALARY_KEYWORDS = "SALARY|WAGE";
        private static final String RENT_KEYWORDS = "RENT";
        private static final String GROCERIES_KEYWORDS = "ALBERT HEIJN|SUPERMARKET|GROCERY|AH";
        
        List<Transaction> parse(String filename) throws IOException {
            List<Transaction> transactions = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    transactions.add(parseLine(line));
                }
            }
            return transactions;
        }
        
        private Transaction parseLine(String line) {
            String[] parts = line.split(";");
            LocalDate date = LocalDate.parse(parts[0]);
            String description = parts[1];
            BigDecimal amount = new BigDecimal(parts[2]);
            Currency currency = Currency.valueOf(parts[3]);
            
            BigDecimal amountEur = amount.multiply(currency.toEur);
            Category category = categorizeDescription(description);
            
            return new Transaction(date, description, amountEur, category, false);
        }
        
        private Category categorizeDescription(String description) {
            String upper = description.toUpperCase();
            if (upper.matches(".*" + SALARY_KEYWORDS + ".*")) return Category.SALARY;
            if (upper.matches(".*" + RENT_KEYWORDS + ".*")) return Category.RENT;
            if (upper.matches(".*" + GROCERIES_KEYWORDS + ".*")) return Category.GROCERIES;
            return Category.OTHER;
        }
    }
    
    static class SuspicionChecker {
        private static final BigDecimal LARGE_THRESHOLD = BigDecimal.valueOf(2000);
        
        List<Transaction> flagSuspicious(List<Transaction> transactions) {
            Map<LocalDate, List<Transaction>> byDate = groupByDate(transactions);
            return transactions.stream()
                .map(t -> isSuspicious(t, byDate) ? withSuspiciousFlag(t) : t)
                .collect(Collectors.toList());
        }
        
        private Map<LocalDate, List<Transaction>> groupByDate(List<Transaction> transactions) {
            return transactions.stream()
                .collect(Collectors.groupingBy(Transaction::date));
        }
        
        private boolean isSuspicious(Transaction t, Map<LocalDate, List<Transaction>> byDate) {
            if (isLargeAmount(t)) return true;
            return hasDuplicateAmountSameDay(t, byDate);
        }
        
        private boolean isLargeAmount(Transaction t) {
            return t.amountEur().abs().compareTo(LARGE_THRESHOLD) > 0;
        }
        
        private boolean hasDuplicateAmountSameDay(Transaction t, Map<LocalDate, List<Transaction>> byDate) {
            long duplicateCount = byDate.get(t.date()).stream()
                .filter(other -> other.amountEur().equals(t.amountEur()))
                .count();
            return duplicateCount > 1;
        }
        
        private Transaction withSuspiciousFlag(Transaction t) {
            return new Transaction(t.date(), t.description(), t.amountEur(), t.category(), true);
        }
    }
    
    static class MonthlyReportGenerator {
        record CategoryTotal(Category category, BigDecimal total) {}
        record MonthSummary(YearMonth month, List<CategoryTotal> totals, List<Transaction> suspicious) {}
        
        List<MonthSummary> generate(List<Transaction> transactions) {
            return transactions.stream()
                .collect(Collectors.groupingBy(t -> YearMonth.from(t.date())))
                .entrySet().stream()
                .map(entry -> summarizeMonth(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(MonthSummary::month))
                .collect(Collectors.toList());
        }
        
        private MonthSummary summarizeMonth(YearMonth month, List<Transaction> monthTransactions) {
            List<CategoryTotal> totals = buildCategoryTotals(monthTransactions);
            List<Transaction> suspicious = filterSuspicious(monthTransactions);
            return new MonthSummary(month, totals, suspicious);
        }
        
        private List<CategoryTotal> buildCategoryTotals(List<Transaction> transactions) {
            return Arrays.stream(Category.values())
                .map(cat -> new CategoryTotal(cat, sumCategory(transactions, cat)))
                .collect(Collectors.toList());
        }
        
        private BigDecimal sumCategory(List<Transaction> transactions, Category category) {
            return transactions.stream()
                .filter(t -> t.category() == category)
                .map(Transaction::amountEur)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        
        private List<Transaction> filterSuspicious(List<Transaction> transactions) {
            return transactions.stream()
                .filter(Transaction::suspicious)
                .collect(Collectors.toList());
        }
    }
    
    static void printReport(List<MonthlyReportGenerator.MonthSummary> report) {
        for (var month : report) {
            System.out.println("\n=== " + month.month() + " ===");
            printCategoryTotals(month.totals());
            printSuspiciousTransactions(month.suspicious());
        }
    }
    
    private static void printCategoryTotals(List<MonthlyReportGenerator.CategoryTotal> totals) {
        for (var total : totals) {
            System.out.printf("%s: %.2f EUR\n", total.category(), total.total());
        }
    }
    
    private static void printSuspiciousTransactions(List<Transaction> suspicious) {
        if (suspicious.isEmpty()) return;
        System.out.println("\nSuspicious Transactions:");
        for (var t : suspicious) {
            System.out.printf("  %s | %s | %.2f EUR\n", t.date(), t.description(), t.amountEur());
        }
    }
    
    public static void main(String[] args) throws IOException {
        List<Transaction> transactions = new StatementParser().parse("statement.txt");
        transactions = new SuspicionChecker().flagSuspicious(transactions);
        List<MonthlyReportGenerator.MonthSummary> report = new MonthlyReportGenerator().generate(transactions);
        printReport(report);
    }
}
