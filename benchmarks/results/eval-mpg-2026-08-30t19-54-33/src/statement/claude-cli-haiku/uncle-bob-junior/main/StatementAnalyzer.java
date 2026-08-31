import java.io.*;
import java.util.*;

class StatementAnalyzer {
    public static void main(String[] args) throws IOException {
        List<Transaction> transactions = readStatements("statement.txt");
        List<TransactionInEur> enriched = transactions.stream()
            .map(TransactionEnricher::enrich)
            .toList();
        
        new MonthlySummary(enriched).printReport();
        
        List<SuspiciousFlag> suspicious = SuspiciousDetector.detect(enriched);
        if (!suspicious.isEmpty()) {
            System.out.println("=== SUSPICIOUS TRANSACTIONS ===\n");
            suspicious.forEach(System.out::println);
        }
    }

    private static List<Transaction> readStatements(String filename) throws IOException {
        List<Transaction> result = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Transaction t = TransactionParser.parse(line);
                if (t != null) result.add(t);
            }
        }
        return result;
    }
}
