import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/** A single order line. Immutable; validated once at construction (the trust boundary). */
record LineItem(String description, int quantity, BigDecimal unitPriceEur) {

    LineItem {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Line item description must not be blank");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive, was: " + quantity);
        }
        if (unitPriceEur == null || unitPriceEur.signum() <= 0) {
            throw new IllegalArgumentException("Unit price must be positive, was: " + unitPriceEur);
        }
    }

    BigDecimal lineTotal() {
        return unitPriceEur.multiply(BigDecimal.valueOf(quantity));
    }
}

/** Monetary breakdown of a processed order. All amounts in EUR, rounded to cents. */
record OrderTotals(
        BigDecimal subtotal,
        BigDecimal discount,
        BigDecimal vat,
        BigDecimal grandTotal
) {}

/**
 * Computes order totals and renders a receipt.
 *
 * Pricing rules: unit prices exclude VAT; a 10% discount applies to the
 * pre-VAT subtotal when it exceeds EUR 100.00; 21% VAT is charged on the
 * discounted amount.
 */
final class OrderProcessor {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD_EUR = new BigDecimal("100.00");
    private static final int CENTS_SCALE = 2;

    private OrderProcessor() {
    }

    static String processOrder(List<LineItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one line item");
        }
        OrderTotals totals = computeTotals(items);
        return renderReceipt(items, totals);
    }

    static OrderTotals computeTotals(List<LineItem> items) {
        BigDecimal subtotal = items.stream()
                .map(LineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(CENTS_SCALE, RoundingMode.HALF_UP);

        BigDecimal discount = discountFor(subtotal);
        BigDecimal discountedSubtotal = subtotal.subtract(discount);
        BigDecimal vat = roundToCents(discountedSubtotal.multiply(VAT_RATE));
        BigDecimal grandTotal = discountedSubtotal.add(vat);

        return new OrderTotals(subtotal, discount, vat, grandTotal);
    }

    private static BigDecimal discountFor(BigDecimal subtotal) {
        if (subtotal.compareTo(DISCOUNT_THRESHOLD_EUR) <= 0) {
            return BigDecimal.ZERO.setScale(CENTS_SCALE, RoundingMode.UNNECESSARY);
        }
        return roundToCents(subtotal.multiply(DISCOUNT_RATE));
    }

    private static BigDecimal roundToCents(BigDecimal amount) {
        return amount.setScale(CENTS_SCALE, RoundingMode.HALF_UP);
    }

    private static String renderReceipt(List<LineItem> items, OrderTotals totals) {
        StringBuilder receipt = new StringBuilder("RECEIPT\n");
        for (LineItem item : items) {
            receipt.append(String.format("%-20s %3d x EUR %8.2f = EUR %9.2f%n",
                    item.description(), item.quantity(), item.unitPriceEur(), item.lineTotal()));
        }
        receipt.append(String.format("%-38s EUR %9.2f%n", "Subtotal (excl. VAT)", totals.subtotal()));
        if (totals.discount().signum() > 0) {
            receipt.append(String.format("%-38s EUR %9.2f%n", "Discount (10%)", totals.discount().negate()));
        }
        receipt.append(String.format("%-38s EUR %9.2f%n", "VAT (21%)", totals.vat()));
        receipt.append(String.format("%-38s EUR %9.2f%n", "TOTAL", totals.grandTotal()));
        return receipt.toString();
    }
}
