import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Represents a single line item on an order.
 */
final class LineItem {

    private final String description;
    private final int quantity;
    private final BigDecimal unitPrice;

    public LineItem(String description, int quantity, BigDecimal unitPrice) {
        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public String getDescription() {
        return description;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getLineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}

/**
 * Thrown when an order fails validation.
 */
class OrderValidationException extends RuntimeException {
    public OrderValidationException(String message) {
        super(message);
    }
}

/**
 * Validates line items, computes totals (with discount and VAT),
 * and produces a receipt for an order.
 */
public class OrderProcessor {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");

    public static class OrderResult {
        public final BigDecimal subtotal;
        public final BigDecimal discount;
        public final BigDecimal discountedSubtotal;
        public final BigDecimal vat;
        public final BigDecimal total;

        OrderResult(BigDecimal subtotal, BigDecimal discount, BigDecimal discountedSubtotal,
                    BigDecimal vat, BigDecimal total) {
            this.subtotal = subtotal;
            this.discount = discount;
            this.discountedSubtotal = discountedSubtotal;
            this.vat = vat;
            this.total = total;
        }
    }

    public void validateLineItems(List<LineItem> lineItems) {
        if (lineItems == null || lineItems.isEmpty()) {
            throw new OrderValidationException("Order must contain at least one line item.");
        }
        for (LineItem item : lineItems) {
            if (item == null) {
                throw new OrderValidationException("Line item cannot be null.");
            }
            if (item.getDescription() == null || item.getDescription().isBlank()) {
                throw new OrderValidationException("Line item description cannot be blank.");
            }
            if (item.getQuantity() <= 0) {
                throw new OrderValidationException(
                        "Line item quantity must be positive: " + item.getDescription());
            }
            if (item.getUnitPrice() == null || item.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new OrderValidationException(
                        "Line item unit price cannot be negative: " + item.getDescription());
            }
        }
    }

    public OrderResult computeTotal(List<LineItem> lineItems) {
        validateLineItems(lineItems);

        BigDecimal subtotal = lineItems.stream()
                .map(LineItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        boolean discountApplies = subtotal.compareTo(DISCOUNT_THRESHOLD) > 0;
        BigDecimal discount = discountApplies
                ? subtotal.multiply(DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal discountedSubtotal = subtotal.subtract(discount);
        BigDecimal vat = discountedSubtotal.multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = discountedSubtotal.add(vat);

        return new OrderResult(subtotal, discount, discountedSubtotal, vat, total);
    }

    public String buildReceipt(List<LineItem> lineItems) {
        OrderResult result = computeTotal(lineItems);
        NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.of("nl", "NL"));

        StringBuilder receipt = new StringBuilder();
        receipt.append("----- RECEIPT -----\n");
        for (LineItem item : lineItems) {
            receipt.append(String.format("%-20s x%-3d %10s%n",
                    item.getDescription(),
                    item.getQuantity(),
                    currency.format(item.getLineTotal())));
        }
        receipt.append("--------------------\n");
        receipt.append(String.format("%-24s %10s%n", "Subtotal:", currency.format(result.subtotal)));
        if (result.discount.compareTo(BigDecimal.ZERO) > 0) {
            receipt.append(String.format("%-24s %10s%n", "Discount (10%):", "-" + currency.format(result.discount)));
        }
        receipt.append(String.format("%-24s %10s%n", "VAT (21%):", currency.format(result.vat)));
        receipt.append(String.format("%-24s %10s%n", "Total:", currency.format(result.total)));
        receipt.append("--------------------\n");

        return receipt.toString();
    }

    public static void main(String[] args) {
        List<LineItem> lineItems = List.of(
                new LineItem("Widget", 3, new BigDecimal("19.99")),
                new LineItem("Gadget", 2, new BigDecimal("24.50"))
        );

        OrderProcessor processor = new OrderProcessor();
        System.out.println(processor.buildReceipt(lineItems));
    }
}
