import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

/**
 * Processes a customer order: validates line items, computes totals
 * (with discount and VAT), and produces a receipt.
 */
public final class OrderProcessor {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final int SCALE = 2;

    public record LineItem(String description, int quantity, BigDecimal unitPrice) {
        BigDecimal lineTotal() {
            return unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    public record OrderTotals(
            BigDecimal subtotal,
            BigDecimal discount,
            BigDecimal discountedSubtotal,
            BigDecimal vat,
            BigDecimal total
    ) {}

    public static final class InvalidOrderException extends RuntimeException {
        public InvalidOrderException(String message) {
            super(message);
        }
    }

    /**
     * Validates that every line item has a non-blank description,
     * a positive quantity, and a non-negative unit price.
     */
    public void validateLineItems(List<LineItem> items) {
        if (items == null || items.isEmpty()) {
            throw new InvalidOrderException("Order must contain at least one line item.");
        }
        for (int i = 0; i < items.size(); i++) {
            LineItem item = items.get(i);
            if (item == null) {
                throw new InvalidOrderException("Line item %d is missing.".formatted(i + 1));
            }
            if (item.description() == null || item.description().isBlank()) {
                throw new InvalidOrderException("Line item %d has no description.".formatted(i + 1));
            }
            if (item.quantity() <= 0) {
                throw new InvalidOrderException(
                        "Line item %d (%s) must have a positive quantity.".formatted(i + 1, item.description()));
            }
            if (item.unitPrice() == null || item.unitPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new InvalidOrderException(
                        "Line item %d (%s) must have a non-negative unit price.".formatted(i + 1, item.description()));
            }
        }
    }

    /**
     * Computes subtotal, discount (10% if pre-VAT subtotal exceeds 100 EUR),
     * VAT (21% on the discounted subtotal), and the final total.
     */
    public OrderTotals computeTotal(List<LineItem> items) {
        BigDecimal subtotal = items.stream()
                .map(LineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        boolean qualifiesForDiscount = subtotal.compareTo(DISCOUNT_THRESHOLD) > 0;
        BigDecimal discount = qualifiesForDiscount
                ? subtotal.multiply(DISCOUNT_RATE)
                : BigDecimal.ZERO;

        BigDecimal discountedSubtotal = subtotal.subtract(discount);
        BigDecimal vat = discountedSubtotal.multiply(VAT_RATE);
        BigDecimal total = discountedSubtotal.add(vat);

        return new OrderTotals(
                round(subtotal),
                round(discount),
                round(discountedSubtotal),
                round(vat),
                round(total)
        );
    }

    /**
     * Produces a human-readable receipt string for the order.
     */
    public String generateReceipt(List<LineItem> items) {
        Objects.requireNonNull(items, "items must not be null");
        validateLineItems(items);
        OrderTotals totals = computeTotal(items);

        StringBuilder receipt = new StringBuilder();
        receipt.append("===== RECEIPT =====\n");
        for (LineItem item : items) {
            receipt.append(String.format("%-20s x%-3d %8s EUR%n",
                    item.description(),
                    item.quantity(),
                    round(item.lineTotal())));
        }
        receipt.append("--------------------\n");
        receipt.append(String.format("%-24s %8s EUR%n", "Subtotal:", totals.subtotal()));
        if (totals.discount().compareTo(BigDecimal.ZERO) > 0) {
            receipt.append(String.format("%-24s -%7s EUR%n", "Discount (10%):", totals.discount()));
        }
        receipt.append(String.format("%-24s %8s EUR%n", "VAT (21%):", totals.vat()));
        receipt.append("====================\n");
        receipt.append(String.format("%-24s %8s EUR%n", "Total:", totals.total()));
        return receipt.toString();
    }

    private static BigDecimal round(BigDecimal value) {
        return value.setScale(SCALE, RoundingMode.HALF_UP);
    }

    public static void main(String[] args) {
        OrderProcessor processor = new OrderProcessor();

        List<LineItem> items = List.of(
                new LineItem("Charity gift card", 2, new BigDecimal("30.00")),
                new LineItem("Lottery ticket bundle", 1, new BigDecimal("45.00"))
        );

        System.out.print(processor.generateReceipt(items));
    }
}
