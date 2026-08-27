import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public final class OrderCalculator {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final int CENTS = 2;

    private OrderCalculator() {
    }

    /** Discount applies to the pre-VAT subtotal; VAT is charged on the discounted net. */
    public static OrderTotals totalsFor(List<LineItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("An order needs at least one line item");
        }
        BigDecimal subtotal = roundToCents(sumOf(items));
        BigDecimal discount = roundToCents(discountOn(subtotal));
        BigDecimal netAmount = subtotal.subtract(discount);
        BigDecimal vat = roundToCents(netAmount.multiply(VAT_RATE));
        return new OrderTotals(subtotal, discount, netAmount, vat, netAmount.add(vat));
    }

    private static BigDecimal sumOf(List<LineItem> items) {
        return items.stream()
                .map(LineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal discountOn(BigDecimal subtotal) {
        boolean qualifiesForDiscount = subtotal.compareTo(DISCOUNT_THRESHOLD) > 0;
        return qualifiesForDiscount ? subtotal.multiply(DISCOUNT_RATE) : BigDecimal.ZERO;
    }

    private static BigDecimal roundToCents(BigDecimal amount) {
        return amount.setScale(CENTS, RoundingMode.HALF_UP);
    }
}
