import java.io.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.regex.Pattern;

public class BankStatementAnalyzer {
    private static final BigDecimal USD_TO_EUR = new BigDecimal("0.92");
    private static final BigDecimal GBP_TO_EUR = new BigDecimal("1.27");
    private static final BigDecimal SUSPICIOUS_THRESHOLD = new BigDecimal("2000");
    private static final String STATEMENT_FILE = "statement.txt";

    static class Transaction {
        final LocalDate date;
        final String description;
        final BigDecimal amountEur;
        final String originalCurrency;
        final Category category;

        Transaction(LocalDate date, String description, BigDecimal amountEur, String originalCurrency, Category category) {
            this.date = date;
            this.description = description;
            this.amountEur = amountEur;
            this.originalCurrency = originalCurrency;
            this.category = category;
        }
    }

    enum Category { SALARY, RENT, GROCERIES, OTHER }

    public static void main(String[] args) throws IOException {
        List<Transaction> transactions = parseTransactions();
        List<Transaction> suspicious = flagSuspicious(transactions);
        Map<YearMonth, CategoryTotals> report = buildMonthlyReport(transactions);
        printReport(report, suspicious);
    }

    static List<Transaction> parseTransactions() throws IOException {
        List<Transaction> transactions = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(STATEMENT_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isBlank()) {
                    Transaction tx = parseLine(line);
                    if (tx != null) transactions.add(tx);
                }
            }
        }
        return transactions;
    }

    static Transaction parseLine(String line) {
        try {
            String[] parts = line.split(";");
            if (parts.length != 4) return null;

            LocalDate date = LocalDate.parse(parts[0]);
            String description = parts[1];
            BigDecimal amount = new BigDecimal(parts[2]);
            String currency = parts[3];

            BigDecimal amountEur = convertToEur(amount, currency);
            Category category = categorize(description);

            return new Transaction(date, description, amountEur, currency, category);
        } catch (Exception e) {
            System.err.println("Failed to parse line: " + line);
            return null;
        }
    }

    static BigDecimal convertToEur(BigDecimal amount, String currency) {
        return switch (currency) {
            case "EUR" -> amount;
            case "USD" -> amount.multiply(USD_TO_EUR);
            case "GBP" -> amount.multiply(GBP_TO_EUR);
            default -> amount;
        };
    }

    static Category categorize(String description) {
        String upper = description.toUpperCase();
        if (matches(upper, "SALARY|WAGE|PAYCHECK|INCOME")) return Category.SALARY;
        if (matches(upper, "RENT|LANDLORD|MORTGAGE")) return Category.RENT;
        if (matches(upper, "ALBERT HEIJN|SUPERMARKET|GROCERY|FOODSTORE")) return Category.GROCERIES;
        return Category.OTHER;
    }

    private static boolean matches(String text, String pattern) {
        return Pattern.compile(pattern).matcher(text).find();
    }

    static List<Transaction> flagSuspicious(List<Transaction> transactions) {
        List<Transaction> suspicious = new ArrayList<>();
        for (Transaction tx : transactions) {
            if (isAboveThreshold(tx) || isRepeatedSameDay(tx, transactions)) {
                suspicious.add(tx);
            }
        }
        return suspicious;
    }

    static boolean isAboveThreshold(Transaction tx) {
        return tx.amountEur.abs().compareTo(SUSPICIOUS_THRESHOLD) > 0;
    }

    static boolean isRepeatedSameDay(Transaction tx, List<Transaction> all) {
        long count = all.stream()
            .filter(t -> t.date.equals(tx.date) && t.amountEur.equals(tx.amountEur))
            .count();
        return count > 1;
    }

    static Map<YearMonth, CategoryTotals> buildMonthlyReport(List<Transaction> transactions) {
        Map<YearMonth, CategoryTotals> report = new TreeMap<>();
        for (Transaction tx : transactions) {
            YearMonth month = YearMonth.from(tx.date);
            CategoryTotals totals = report.computeIfAbsent(month, k -> new CategoryTotals());
            totals.add(tx.category, tx.amountEur);
        }
        return report;
    }

    private static void printReport(Map<YearMonth, CategoryTotals> report, List<Transaction> suspicious) {
        System.out.println("=== MONTHLY STATEMENT REPORT ===\n");
        for (Map.Entry<YearMonth, CategoryTotals> entry : report.entrySet()) {
            System.out.println(entry.getKey());
            entry.getValue().print();
            System.out.println();
        }

        if (!suspicious.isEmpty()) {
            System.out.println("=== SUSPICIOUS TRANSACTIONS ===");
            for (Transaction tx : suspicious) {
                System.out.printf("%s | %s | %.2f EUR | %s\n", tx.date, tx.description, tx.amountEur, tx.category);
            }
        }
    }

    static class CategoryTotals {
        private final Map<Category, BigDecimal> totals = new EnumMap<>(Category.class);

        void add(Category cat, BigDecimal amount) {
            totals.put(cat, totals.getOrDefault(cat, BigDecimal.ZERO).add(amount));
        }

        void print() {
            for (Category cat : Category.values()) {
                BigDecimal total = totals.getOrDefault(cat, BigDecimal.ZERO);
                System.out.printf("  %s: %.2f EUR\n", cat, total);
            }
        }
    }
}
