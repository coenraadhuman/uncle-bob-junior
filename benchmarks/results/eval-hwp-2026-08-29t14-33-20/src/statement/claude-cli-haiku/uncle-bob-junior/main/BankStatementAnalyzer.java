import java.io.*;
import java.util.*;
import java.util.stream.*;

public class BankStatementAnalyzer {
    public static void main(String[] args) throws IOException {
        new BankStatementAnalyzer().run();
    }
    
    private void run() throws IOException {
        var transactions = readTransactions();
        var analyzed = analyzeTransactions(transactions);
        new MonthlyReporter().report(analyzed);
    }
    
    private List<String> readTransactions() throws IOException {
        try (var reader = new BufferedReader(new FileReader("statement.txt"))) {
            return reader.lines()
                .filter(line -> !line.trim().isEmpty())
                .collect(Collectors.toList());
        }
    }
    
    private List<Transaction> analyzeTransactions(List<String> lines) {
        var parsed = lines.stream().map(Transaction::parse).collect(Collectors.toList());
        return new SuspiciousFlagService().flagAll(parsed);
    }
}
