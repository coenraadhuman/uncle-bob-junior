// OrderProcessor.java
import java.math.BigDecimal;
import java.math.RoundingMode;

public final class OrderProcessor {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final int MONEY_SCALE = 2;

    private OrderProcessor() {
    }

    public static Receipt process(Order order) {
        BigDecimal subtotal = calculateSubtotal(order);
        BigDecimal discount = calculateDiscount(subtotal);
        BigDecimal discountedSubtotal = subtotal.subtract(discount);
        BigDecimal vat = round(discountedSubtotal.multiply(VAT_RATE));
        BigDecimal total = round(discountedSubtotal.add(vat));
        String text = ReceiptFormatter.format(order, subtotal, discount, vat, total);
        return new Receipt(subtotal, discount, vat, total, text);
    }

    private static BigDecimal calculateSubtotal(Order order) {
        BigDecimal sum = order.items().stream()
                .map(LineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return round(sum);
    }

    private static BigDecimal calculateDiscount(BigDecimal subtotal) {
        boolean qualifiesForDiscount = subtotal.compareTo(DISCOUNT_THRESHOLD) > 0;
        if (!qualifiesForDiscount) {
            return round(BigDecimal.ZERO);
        }
        return round(subtotal.multiply(DISCOUNT_RATE));
    }

    private static BigDecimal round(BigDecimal value) {
        return value.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
