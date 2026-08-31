import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

class RefundPolicy {
    private static final int FULL_REFUND_DAYS = 30;
    private static final int PARTIAL_REFUND_DAYS = 7;
    
    public double calculateRefund(double ticketPrice, LocalDateTime eventTime, LocalDateTime now) {
        long daysUntilEvent = ChronoUnit.DAYS.between(now, eventTime);
        
        if (daysUntilEvent > FULL_REFUND_DAYS) {
            return ticketPrice;
        }
        if (daysUntilEvent > PARTIAL_REFUND_DAYS) {
            return ticketPrice * 0.5;
        }
        return 0;
    }
}
