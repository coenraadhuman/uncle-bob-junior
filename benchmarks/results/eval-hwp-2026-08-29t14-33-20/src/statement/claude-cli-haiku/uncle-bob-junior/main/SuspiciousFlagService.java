import java.util.*;
import java.util.stream.*;

class SuspiciousFlagService {
    private static final double LARGE_AMOUNT = 2000.0;
    
    List<Transaction> flagAll(List<Transaction> transactions) {
        var flaggedLarge = flagLargeAmounts(transactions);
        return flagDuplicateDayAmounts(flaggedLarge);
    }
    
    private List<Transaction> flagLargeAmounts(List<Transaction> transactions) {
        return transactions.stream()
            .map(t -> Math.abs(t.amountEur()) > LARGE_AMOUNT ? t.withSuspicious() : t)
            .collect(Collectors.toList());
    }
    
    private List<Transaction> flagDuplicateDayAmounts(List<Transaction> transactions) {
        var duplicates = findDuplicateDayAmounts(transactions);
        return transactions.stream()
            .map(t -> duplicates.contains(dateAmountKey(t)) ? t.withSuspicious() : t)
            .collect(Collectors.toList());
    }
    
    private Set<DateAmountKey> findDuplicateDayAmounts(List<Transaction> transactions) {
        return transactions.stream()
            .collect(Collectors.groupingBy(this::dateAmountKey))
            .entrySet().stream()
            .filter(e -> e.getValue().size() > 1)
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());
    }
    
    private DateAmountKey dateAmountKey(Transaction t) {
        return new DateAmountKey(t.date(), t.amountEur());
    }
}
