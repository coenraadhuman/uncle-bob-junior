// OrderCalculator.java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

public final class OrderCalculator {

    static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final int MONEY_SCALE = 2;

    public OrderSummary summarize(List<LineItem> lineItems) {
        validateOrder(lineItems);
        BigDecimal subtotal = computeSubtotal(lineItems);
        BigDecimal discount = computeDiscount(subtotal);
        BigDecimal netAfterDiscount = round(subtotal.subtract(discount));
        BigDecimal vatAmount = computeVat(netAfterDiscount);
        BigDecimal total = round(netAfterDiscount.add(vatAmount));
        return new OrderSummary(round(subtotal), discount, vatAmount, total);
    }

    private void validateOrder(List<LineItem> lineItems) {
        Objects.requireNonNull(lineItems, "lineItems must not be null");
        if (lineItems.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one line item");
        }
    }

    private BigDecimal computeSubtotal(List<LineItem> lineItems) {
        return lineItems.stream()
            .map(LineItem::lineTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal computeDiscount(BigDecimal subtotal) {
        if (subtotal.compareTo(DISCOUNT_THRESHOLD) <= 0) {
            return round(BigDecimal.ZERO);
        }
        return round(subtotal.multiply(DISCOUNT_RATE));
    }

    private BigDecimal computeVat(BigDecimal netAmount) {
        return round(netAmount.multiply(VAT_RATE));
    }

    private BigDecimal round(BigDecimal amount) {
        return amount.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
