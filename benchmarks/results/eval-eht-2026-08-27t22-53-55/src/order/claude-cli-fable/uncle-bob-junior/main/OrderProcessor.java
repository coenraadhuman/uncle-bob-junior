import java.math.BigDecimal;
import java.math.RoundingMode;

/** Prices an order: subtotal, threshold discount, VAT, and grand total. */
public final class OrderProcessor {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final int CENTS = 2;

    public Receipt process(Order order) {
        BigDecimal subtotal = roundToCents(subtotalOf(order));
        BigDecimal discount = discountFor(subtotal);
        BigDecimal discountedSubtotal = subtotal.subtract(discount);
        BigDecimal vat = roundToCents(discountedSubtotal.multiply(VAT_RATE));
        BigDecimal total = discountedSubtotal.add(vat);
        return new Receipt(order.items(), subtotal, discount, vat, total);
    }

    private static BigDecimal subtotalOf(Order order) {
        return order.items().stream()
                .map(LineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal discountFor(BigDecimal subtotal) {
        if (subtotal.compareTo(DISCOUNT_THRESHOLD) <= 0) {
            return BigDecimal.ZERO.setScale(CENTS);
        }
        return roundToCents(subtotal.multiply(DISCOUNT_RATE));
    }

    private static BigDecimal roundToCents(BigDecimal amount) {
        return amount.setScale(CENTS, RoundingMode.HALF_UP);
    }
}
