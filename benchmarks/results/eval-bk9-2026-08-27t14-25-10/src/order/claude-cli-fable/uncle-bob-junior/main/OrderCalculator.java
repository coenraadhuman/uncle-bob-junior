import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/** Pure calculation of order totals: no I/O, no mutation. */
public final class OrderCalculator {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final int CENT_SCALE = 2;

    public OrderTotals totalsFor(List<LineItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("An order needs at least one line item");
        }
        BigDecimal subtotal = roundToCents(subtotalOf(items));
        BigDecimal discount = discountOn(subtotal);
        // ubj: business rule per spec, VAT is charged on the discounted amount
        BigDecimal taxable = subtotal.subtract(discount);
        BigDecimal vat = roundToCents(taxable.multiply(VAT_RATE));
        return new OrderTotals(subtotal, discount, vat, taxable.add(vat));
    }

    private BigDecimal subtotalOf(List<LineItem> items) {
        return items.stream()
                .map(LineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal discountOn(BigDecimal subtotal) {
        if (subtotal.compareTo(DISCOUNT_THRESHOLD) <= 0) {
            return BigDecimal.ZERO.setScale(CENT_SCALE, RoundingMode.HALF_UP);
        }
        return roundToCents(subtotal.multiply(DISCOUNT_RATE));
    }

    private static BigDecimal roundToCents(BigDecimal amount) {
        return amount.setScale(CENT_SCALE, RoundingMode.HALF_UP);
    }
}
