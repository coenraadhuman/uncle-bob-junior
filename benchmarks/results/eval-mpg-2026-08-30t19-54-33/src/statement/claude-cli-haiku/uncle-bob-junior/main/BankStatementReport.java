import java.time.*;
import java.util.*;
import java.util.stream.*;

class BankStatementReport {
    private final List<Transaction> transactions;

    BankStatementReport(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    void print() {
        printMonthlyReport();
        printSuspiciousTransactions();
    }

    private void printMonthlyReport() {
        Map<YearMonth, Map<TransactionCategory, Double>> monthlyCategoryTotals =
            calculateMonthlyCategoryTotals();

        List<YearMonth> months = monthlyCategoryTotals.keySet().stream()
            .sorted()
            .toList();

        for (YearMonth month : months) {
            printMonth(month, monthlyCategoryTotals.get(month));
        }
    }

    private Map<YearMonth, Map<TransactionCategory, Double>> calculateMonthlyCategoryTotals() {
        return transactions.stream()
            .collect(Collectors.groupingBy(
                Transaction::month,
                Collectors.groupingBy(
                    Transaction::category,
                    Collectors.summingDouble(Transaction::amountEur)
                )
            ));
    }

    private void printMonth(YearMonth month, Map<TransactionCategory, Double> totals) {
        System.out.println("\n" + month);
        System.out.println("--------");
        for (TransactionCategory category : TransactionCategory.values()) {
            double total = totals.getOrDefault(category, 0.0);
            System.out.printf("%s: €%.2f%n", category, total);
        }
    }

    private void printSuspiciousTransactions() {
        List<Transaction> suspicious = transactions.stream()
            .filter(Transaction::isSuspicious)
            .sorted(Comparator.comparing(Transaction::date))
            .toList();

        if (!suspicious.isEmpty()) {
            System.out.println("\n\nSuspicious Transactions");
            System.out.println("======================");
            for (Transaction t : suspicious) {
                System.out.printf("%s | %s | €%.2f | %s%n",
                    t.date(), t.description(), t.amountEur(), t.category());
            }
        }
    }
}
