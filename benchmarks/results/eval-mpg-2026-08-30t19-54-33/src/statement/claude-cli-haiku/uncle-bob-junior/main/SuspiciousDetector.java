import java.math.BigDecimal;
import java.util.*;

class SuspiciousDetector {
    private static final BigDecimal HIGH_THRESHOLD = new BigDecimal("2000");

    static List<SuspiciousFlag> detect(List<TransactionInEur> enriched) {
        List<SuspiciousFlag> flags = new ArrayList<>();
        
        for (TransactionInEur t : enriched) {
            String reasons = buildReasons(t, enriched);
            if (!reasons.isEmpty()) {
                flags.add(new SuspiciousFlag(t.getDate(), t.getDescription(), t.getAmount(), reasons));
            }
        }
        
        return flags;
    }

    private static String buildReasons(TransactionInEur t, List<TransactionInEur> all) {
        List<String> reasons = new ArrayList<>();
        
        if (t.getAmount().compareTo(HIGH_THRESHOLD) > 0) {
            reasons.add("Amount exceeds 2000 EUR");
        }
        
        if (hasDuplicateOnSameDay(t, all)) {
            reasons.add("Repeated identical amount on same day");
        }
        
        return String.join("; ", reasons);
    }

    private static boolean hasDuplicateOnSameDay(TransactionInEur t, List<TransactionInEur> all) {
        return all.stream()
            .anyMatch(other -> other.getDate().equals(t.getDate()) &&
                             other.getAmount().compareTo(t.getAmount()) == 0 &&
                             !other.getDescription().equals(t.getDescription()));
    }
}
