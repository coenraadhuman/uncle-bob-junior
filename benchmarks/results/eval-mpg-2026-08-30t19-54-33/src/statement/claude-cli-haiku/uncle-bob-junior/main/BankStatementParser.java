import java.io.*;
import java.time.*;
import java.util.*;

class BankStatementParser {
    private static final double SUSPICIOUS_AMOUNT_THRESHOLD = 2000.0;
    private final CurrencyConverter converter;
    private final TransactionCategorizer categorizer;

    BankStatementParser(CurrencyConverter converter, TransactionCategorizer categorizer) {
        this.converter = converter;
        this.categorizer = categorizer;
    }

    List<Transaction> parse(String filename) throws IOException {
        List<RawTransaction> raw = readRaw(filename);
        return enrich(raw);
    }

    private List<RawTransaction> readRaw(String filename) throws IOException {
        List<RawTransaction> raw = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    raw.add(parseRaw(line));
                }
            }
        }
        return raw;
    }

    private RawTransaction parseRaw(String line) {
        String[] parts = line.split(";");
        LocalDate date = LocalDate.parse(parts[0]);
        String description = parts[1];
        double amount = Double.parseDouble(parts[2]);
        String currency = parts[3];
        double amountEur = converter.toEur(amount, currency);
        return new RawTransaction(date, description, amountEur);
    }

    private List<Transaction> enrich(List<RawTransaction> raw) {
        Map<LocalDate, Map<Double, Integer>> dayAmounts = new HashMap<>();
        List<Transaction> transactions = new ArrayList<>();

        for (RawTransaction r : raw) {
            boolean isDuplicate = markAmountOnDay(r.date(), r.amountEur(), dayAmounts);
            TransactionCategory category = categorizer.categorize(r.description());
            boolean suspicious = r.amountEur() > SUSPICIOUS_AMOUNT_THRESHOLD || isDuplicate;

            transactions.add(new Transaction(r.date(), r.description(), r.amountEur(),
                                             category, suspicious));
        }
        return transactions;
    }

    private boolean markAmountOnDay(LocalDate date, double amount,
                                    Map<LocalDate, Map<Double, Integer>> dayAmounts) {
        Map<Double, Integer> amounts = dayAmounts.computeIfAbsent(date, k -> new HashMap<>());
        int count = amounts.getOrDefault(amount, 0);
        amounts.put(amount, count + 1);
        return count > 0;
    }
}
