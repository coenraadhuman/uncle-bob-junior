import java.io.*;
import java.time.*;
import java.util.*;
import java.util.stream.*;

public class BankStatementAnalyzer {
    
    public static void main(String[] args) throws IOException {
        if (args.length > 0 && "test".equals(args[0])) {
            BankStatementAnalyzerTest.runTests();
            return;
        }
        
        List<Transaction> transactions = readTransactions("statement.txt");
        Map<YearMonth, MonthlyReport> report = generateReport(transactions);
        printReport(report);
    }
    
    private static List<Transaction> readTransactions(String filename) throws IOException {
        List<Transaction> transactions = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    transactions.add(parseTransaction(line));
                }
            }
        }
        return transactions;
    }
    
    private static Transaction parseTransaction(String line) {
        String[] parts = line.split(";");
        LocalDate date = LocalDate.parse(parts[0]);
        String description = parts[1];
        double amount = Double.parseDouble(parts[2]);
        String currency = parts[3];
        double amountEur = CurrencyConverter.toEur(amount, currency);
        String category = Categorizer.categorize(description);
        return new Transaction(date, description, amount, currency, amountEur, category);
    }
    
    private static Map<YearMonth, MonthlyReport> generateReport(List<Transaction> transactions) {
        Map<YearMonth, MonthlyReport> report = new TreeMap<>();
        for (Transaction transaction : transactions) {
            YearMonth month = YearMonth.from(transaction.date());
            MonthlyReport monthReport = report.computeIfAbsent(month, MonthlyReport::new);
            monthReport.addTransaction(transaction);
        }
        return report;
    }
    
    private static void printReport(Map<YearMonth, MonthlyReport> report) {
        report.values().forEach(MonthlyReport::print);
    }
}
