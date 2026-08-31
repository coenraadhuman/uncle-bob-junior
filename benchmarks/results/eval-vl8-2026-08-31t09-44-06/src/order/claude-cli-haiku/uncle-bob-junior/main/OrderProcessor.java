import java.math.BigDecimal;
import java.math.RoundingMode;

class OrderProcessor {
    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    
    Receipt process(Order order) {
        BigDecimal subtotal = computeSubtotal(order);
        BigDecimal discountAmount = applyDiscount(subtotal);
        BigDecimal afterDiscount = subtotal.subtract(discountAmount);
        BigDecimal vatAmount = afterDiscount.multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = afterDiscount.add(vatAmount);
        
        return new Receipt(order, subtotal, discountAmount, vatAmount, total);
    }
    
    private BigDecimal computeSubtotal(Order order) {
        return order.items().stream()
            .map(LineItem::lineTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);
    }
    
    private BigDecimal applyDiscount(BigDecimal subtotal) {
        if (subtotal.compareTo(DISCOUNT_THRESHOLD) > 0) {
            return subtotal.multiply(DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }
}
