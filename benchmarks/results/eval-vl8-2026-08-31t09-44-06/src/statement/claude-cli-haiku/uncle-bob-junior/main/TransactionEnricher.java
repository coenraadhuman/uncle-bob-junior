class TransactionEnricher {
    private final Categorizer categorizer;
    
    TransactionEnricher(Categorizer categorizer) {
        this.categorizer = categorizer;
    }
    
    Transaction enrich(Transaction transaction) {
        Category category = categorizer.categorise(transaction.description());
        return new Transaction(transaction.date(), transaction.description(), 
                              transaction.amountEur(), category);
    }
}
