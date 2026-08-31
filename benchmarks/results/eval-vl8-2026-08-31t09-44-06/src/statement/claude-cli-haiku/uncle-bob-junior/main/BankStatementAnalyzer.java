import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

public class BankStatementAnalyzer {
    private static final double USD_TO_EUR = 0.92;
    private static final double GBP_TO_EUR = 1.17;
    private static final double SUSPICIOUS_THRESHOLD = 2000.0;
    
    private static final Map<String, String> CATEGORY_KEYWORDS = Map.ofEntries(
        Map.entry("salary", "salary|wage|bonus|income"),
        Map.entry("rent", "rent|landlord|housing"),
        Map.entry("groceries", "albert heijn|supermarket|grocery|lidl|jumbo")
    );
    
    public static void main(String[] args) throws IOException {
        List<Transaction> transactions = parseStatements(Path.of("statement.txt"));
        Map<YearMonth, MonthlyReport> reportsByMonth = generateReportsByMonth(transactions);
        printReport(reportsByMonth);
    }
    
    private static List<Transaction> parseStatements(Path filePath) throws IOException {
        return Files.lines(filePath)
            .map(Transaction::parse)
            .collect(Collectors.toList());
    }
    
    private static Map<YearMonth, MonthlyReport> generateReportsByMonth(List<Transaction> transactions) {
        Map<YearMonth, List<Transaction>> groupedByMonth = transactions.stream()
            .collect(Collectors.groupingBy(t -> YearMonth.from(t.date())));
        
        return groupedByMonth.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> new MonthlyReport(e.getKey(), e.getValue())
            ));
    }
    
    private static void printReport(Map<YearMonth, MonthlyReport> reportsByMonth) {
        reportsByMonth.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(e -> System.out.println(e.getValue()));
    }
}
