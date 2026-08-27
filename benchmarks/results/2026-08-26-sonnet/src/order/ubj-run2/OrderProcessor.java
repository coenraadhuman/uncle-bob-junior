import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public final class OrderProcessor {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final int MONEY_SCALE = 2;

    public Receipt process(List<LineItem> lineItems) {
        validateLineItems(lineItems);

        BigDecimal subtotal = computeSubtotal(lineItems);
        BigDecimal discount = computeDiscount(subtotal);
        BigDecimal discountedSubtotal = subtotal.subtract(discount);
        BigDecimal vat = computeVat(discountedSubtotal);
        BigDecimal total = discountedSubtotal.add(vat);

        return new Receipt(lineItems, subtotal, discount, vat, total);
    }

    private void validateLineItems(List<LineItem> lineItems) {
        if (lineItems == null || lineItems.isEmpty()) {
            throw new InvalidOrderException("Order must contain at least one line item.");
        }
        lineItems.forEach(this::validateLineItem);
    }

    private void validateLineItem(LineItem lineItem) {
        if (lineItem == null) {
            throw new InvalidOrderException("Line item must not be null.");
        }
        if (lineItem.description() == null || lineItem.description().isBlank()) {
            throw new InvalidOrderException("Line item description must not be blank.");
        }
        if (lineItem.quantity() <= 0) {
            throw new InvalidOrderException(
                    "Line item quantity must be positive: " + lineItem.description());
        }
        if (lineItem.unitPrice() == null || lineItem.unitPrice().signum() < 0) {
            throw new InvalidOrderException(
                    "Line item unit price must not be negative: " + lineItem.description());
        }
    }

    private BigDecimal computeSubtotal(List<LineItem> lineItems) {
        return lineItems.stream()
                .map(LineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal computeDiscount(BigDecimal subtotal) {
        if (subtotal.compareTo(DISCOUNT_THRESHOLD) <= 0) {
            return BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        }
        return subtotal.multiply(DISCOUNT_RATE).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal computeVat(BigDecimal discountedSubtotal) {
        return discountedSubtotal.multiply(VAT_RATE).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
