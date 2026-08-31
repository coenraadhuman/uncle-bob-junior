import java.math.BigDecimal;
import java.math.RoundingMode;

record Money(BigDecimal amount) {
    private static final BigDecimal GROUP_DISCOUNT_FACTOR = new BigDecimal("0.95");
    private static final BigDecimal PARTIAL_REFUND_FACTOR = new BigDecimal("0.50");
    
    public Money multiply(BigDecimal factor) {
        return new Money(amount.multiply(factor).setScale(2, RoundingMode.HALF_UP));
    }
    
    public Money add(Money other) {
        return new Money(amount.add(other.amount));
    }
    
    public Money applyGroupDiscount() {
        return multiply(GROUP_DISCOUNT_FACTOR);
    }
    
    public Money applyPartialRefund() {
        return multiply(PARTIAL_REFUND_FACTOR);
    }
}
