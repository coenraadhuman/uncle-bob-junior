import java.math.BigDecimal;
import java.util.List;

public class OrderProcessor {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100");

    public Receipt processOrder(List<LineItem> lineItems) {
        if (lineItems == null || lineItems.isEmpty()) {
            throw new InvalidOrderException("Order must contain at least one line item");
        }

        BigDecimal subtotal = calculateSubtotal(lineItems);
        BigDecimal discount = calculateDiscount(subtotal);
        BigDecimal discountedSubtotal = subtotal.subtract(discount);
        BigDecimal vatAmount = calculateVat(discountedSubtotal);
        BigDecimal total = discountedSubtotal.add(vatAmount);

        return new Receipt(
                Money.round(subtotal),
                Money.round(discount),
                Money.round(vatAmount),
                Money.round(total));
    }

    private BigDecimal calculateSubtotal(List<LineItem> lineItems) {
        return lineItems.stream()
                .map(LineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateDiscount(BigDecimal subtotal) {
        boolean isEligibleForDiscount = subtotal.compareTo(DISCOUNT_THRESHOLD) > 0;
        return isEligibleForDiscount ? subtotal.multiply(DISCOUNT_RATE) : BigDecimal.ZERO;
    }

    private BigDecimal calculateVat(BigDecimal amount) {
        return amount.multiply(VAT_RATE);
    }
}
