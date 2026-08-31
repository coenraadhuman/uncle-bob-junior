// RefundPolicy.java
public final class RefundPolicy {
    private static final long MILLIS_PER_DAY = 24 * 60 * 60 * 1000L;
    private static final int DAYS_FULL_REFUND = 30;
    private static final int DAYS_HALF_REFUND = 7;

    public Money calculateRefund(Money originalPrice, long eventDateMillis, long nowMillis) {
        long daysUntilEvent = (eventDateMillis - nowMillis) / MILLIS_PER_DAY;
        return calculateRefundByDays(originalPrice, daysUntilEvent);
    }

    private Money calculateRefundByDays(Money originalPrice, long daysUntilEvent) {
        if (daysUntilEvent > DAYS_FULL_REFUND) {
            return originalPrice;
        }
        if (daysUntilEvent > DAYS_HALF_REFUND) {
            return originalPrice.multiply(0.5);
        }
        return new Money(0);
    }
}
