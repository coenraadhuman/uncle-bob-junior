// OrderProcessor.java
import java.math.BigDecimal;
import java.math.RoundingMode;

public final class OrderProcessor {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final int MONEY_SCALE = 2;

    public OrderReceipt process(Order order) {
        BigDecimal subtotal = calculateSubtotal(order);
        BigDecimal discount = calculateDiscount(subtotal);
        BigDecimal discountedSubtotal = subtotal.subtract(discount);
        BigDecimal vat = calculateVat(discountedSubtotal);
        BigDecimal total = discountedSubtotal.add(vat);
        return new OrderReceipt(order.lineItems(), subtotal, discount, vat, total);
    }

    private BigDecimal calculateSubtotal(Order order) {
        return order.lineItems().stream()
                .map(LineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateDiscount(BigDecimal subtotal) {
        if (subtotal.compareTo(DISCOUNT_THRESHOLD) <= 0) {
            return BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        }
        return subtotal.multiply(DISCOUNT_RATE).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateVat(BigDecimal discountedSubtotal) {
        return discountedSubtotal.multiply(VAT_RATE).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
