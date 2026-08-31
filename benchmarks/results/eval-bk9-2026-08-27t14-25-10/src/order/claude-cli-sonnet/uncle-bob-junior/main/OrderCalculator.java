public final class OrderCalculator {

    static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final int MONEY_SCALE = 2;

    private OrderCalculator() {
    }

    public static OrderTotals computeTotals(List<LineItem> lineItems) {
        requireNonEmpty(lineItems);
        BigDecimal subtotal = sumLineTotals(lineItems);
        BigDecimal discount = calculateDiscount(subtotal);
        BigDecimal discountedSubtotal = subtotal.subtract(discount);
        BigDecimal vat = round(discountedSubtotal.multiply(VAT_RATE));
        BigDecimal total = discountedSubtotal.add(vat);
        return new OrderTotals(round(subtotal), discount, vat, round(total));
    }

    private static void requireNonEmpty(List<LineItem> lineItems) {
        if (lineItems == null || lineItems.isEmpty()) {
            throw new InvalidLineItemException("Order must contain at least one line item");
        }
    }

    private static BigDecimal sumLineTotals(List<LineItem> lineItems) {
        return lineItems.stream()
                .map(LineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal calculateDiscount(BigDecimal subtotal) {
        boolean qualifiesForDiscount = subtotal.compareTo(DISCOUNT_THRESHOLD) > 0;
        return qualifiesForDiscount ? round(subtotal.multiply(DISCOUNT_RATE)) : BigDecimal.ZERO.setScale(MONEY_SCALE);
    }

    private static BigDecimal round(BigDecimal value) {
        return value.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}

// ---- ReceiptFormatter.java ----
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
