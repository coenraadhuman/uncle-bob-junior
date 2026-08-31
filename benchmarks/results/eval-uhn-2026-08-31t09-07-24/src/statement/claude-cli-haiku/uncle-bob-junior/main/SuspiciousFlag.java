class SuspiciousFlag {
    final Transaction transaction;
    final String reason;
    
    SuspiciousFlag(Transaction transaction, String reason) {
        this.transaction = transaction;
        this.reason = reason;
    }
}
