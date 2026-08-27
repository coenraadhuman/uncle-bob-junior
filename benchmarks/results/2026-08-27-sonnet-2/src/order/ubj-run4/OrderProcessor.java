// File: OrderProcessor.java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public final class OrderProcessor {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD_EUROS = new BigDecimal("100");
    private static final int EURO_SCALE = 2;

    public OrderTotals calculateTotals(List<LineItem> lineItems) {
        requireNonEmpty(lineItems);

        BigDecimal subtotal = sumLineItems(lineItems);
        BigDecimal discount = calculateDiscount(subtotal);
        BigDecimal netOfDiscount = subtotal.subtract(discount);
        BigDecimal vat = calculateVat(netOfDiscount);
        BigDecimal total = netOfDiscount.add(vat);

        return new OrderTotals(round(subtotal), round(discount), round(vat), round(total));
    }

    private void requireNonEmpty(List<LineItem> lineItems) {
        if (lineItems == null || lineItems.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one line item");
        }
    }

    private BigDecimal sumLineItems(List<LineItem> lineItems) {
        return lineItems.stream()
                .map(LineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateDiscount(BigDecimal subtotal) {
        boolean qualifiesForDiscount = subtotal.compareTo(DISCOUNT_THRESHOLD_EUROS) > 0;
        return qualifiesForDiscount ? subtotal.multiply(DISCOUNT_RATE) : BigDecimal.ZERO;
    }

    private BigDecimal calculateVat(BigDecimal netAmount) {
        return netAmount.multiply(VAT_RATE);
    }

    private BigDecimal round(BigDecimal value) {
        return value.setScale(EURO_SCALE, RoundingMode.HALF_UP);
    }
}
