import java.math.BigDecimal;

class TransactionEnricher {
    static TransactionInEur enrich(Transaction t) {
        BigDecimal eurAmount = ExchangeRates.convertToEur(t.getAmount(), t.getCurrency());
        String category = CategoryClassifier.categorize(t.getDescription());
        return new TransactionInEur(t.getDate(), t.getDescription(), eurAmount, category);
    }
}
