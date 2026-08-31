import java.math.BigDecimal;
import java.time.ChronoUnit;
import java.time.LocalDateTime;

class RefundCalculator {
    private static final int FULL_REFUND_THRESHOLD_DAYS = 30;
    private static final int PARTIAL_REFUND_THRESHOLD_DAYS = 7;

    BigDecimal calculateRefund(BigDecimal paidAmount, LocalDateTime eventDate, LocalDateTime cancelDate) {
        long daysUntilEvent = ChronoUnit.DAYS.between(cancelDate, eventDate);
        
        if (daysUntilEvent > FULL_REFUND_THRESHOLD_DAYS) {
            return paidAmount;
        }
        if (daysUntilEvent > PARTIAL_REFUND_THRESHOLD_DAYS) {
            return paidAmount.multiply(new BigDecimal("0.50"));
        }
        return BigDecimal.ZERO;
    }
}
