import java.math.BigDecimal;
import java.math.RoundingMode;

public class OrderProcessor {
    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100");
    private static final int SCALE = 2;
    
    public OrderReceipt process(Order order) {
        BigDecimal subtotal = calculateSubtotal(order);
        BigDecimal discount = calculateDiscount(subtotal);
        BigDecimal discountedAmount = subtotal.subtract(discount);
        BigDecimal vatAmount = calculateVat(discountedAmount);
        BigDecimal total = discountedAmount.add(vatAmount);
        
        return new OrderReceipt(order.getItems(), subtotal, discount, vatAmount, total);
    }
    
    private BigDecimal calculateSubtotal(Order order) {
        return order.getItems().stream()
            .map(LineItem::lineTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    private BigDecimal calculateDiscount(BigDecimal subtotal) {
        if (subtotal.compareTo(DISCOUNT_THRESHOLD) > 0) {
            return subtotal.multiply(DISCOUNT_RATE)
                .setScale(SCALE, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }
    
    private BigDecimal calculateVat(BigDecimal amount) {
        return amount.multiply(VAT_RATE)
            .setScale(SCALE, RoundingMode.HALF_UP);
    }
}
