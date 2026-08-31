import java.math.BigDecimal;
import java.util.*;

class SuspiciousDetector {
    private static final BigDecimal LARGE_THRESHOLD = new BigDecimal("2000");
    
    boolean isLarge(Transaction transaction) {
        return transaction.amountEur().abs().compareTo(LARGE_THRESHOLD) > 0;
    }
    
    boolean hasDuplicateOnDay(Transaction current, List<Transaction> transactions) {
        return transactions.stream()
            .filter(t -> t != current)
            .filter(t -> t.date().equals(current.date()))
            .anyMatch(t -> t.amountEur().compareTo(current.amountEur()) == 0);
    }
}
