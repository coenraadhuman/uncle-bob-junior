import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** A single line item on an order, before tax or discount. */
final class OrderLine {

    private final String description;
    private final int quantity;
    private final BigDecimal unitPrice;

    public OrderLine(String description, int quantity, BigDecimal unitPrice) {
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

    public BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}

/** Thrown when an order or one of its line items fails validation. */
final class OrderValidationException extends RuntimeException {
    public OrderValidationException(String message) {
        super(message);
    }
}

/** Validates orders, applies discount and VAT, and renders a receipt. */
final class OrderProcessor {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");

    public String processOrder(String orderId, List<OrderLine> lines) {
        validateLines(lines);

        BigDecimal subtotal = lines.stream()
                .map(OrderLine::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        boolean discountApplies = subtotal.compareTo(DISCOUNT_THRESHOLD) > 0;
        BigDecimal discount = discountApplies
                ? subtotal.multiply(DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal discountedSubtotal = subtotal.subtract(discount);
        BigDecimal vat = discountedSubtotal.multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = discountedSubtotal.add(vat);

        return buildReceipt(orderId, lines, subtotal, discount, discountedSubtotal, vat, total);
    }

    private void validateLines(List<OrderLine> lines) {
        if (lines == null || lines.isEmpty()) {
            throw new OrderValidationException("Order must contain at least one line item.");
        }
        for (OrderLine line : lines) {
            if (line.getDescription() == null || line.getDescription().isBlank()) {
                throw new OrderValidationException("Line item description must not be blank.");
            }
            if (line.getQuantity() <= 0) {
                throw new OrderValidationException(
                        "Line item '" + line.getDescription() + "' must have a quantity greater than zero.");
            }
            if (line.getUnitPrice() == null || line.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new OrderValidationException(
                        "Line item '" + line.getDescription() + "' must have a non-negative unit price.");
            }
        }
    }

    private String buildReceipt(String orderId, List<OrderLine> lines, BigDecimal subtotal,
                                 BigDecimal discount, BigDecimal discountedSubtotal,
                                 BigDecimal vat, BigDecimal total) {
        StringBuilder sb = new StringBuilder();
        sb.append("Receipt for order ").append(orderId).append("\n");
        sb.append("-".repeat(40)).append("\n");

        for (OrderLine line : lines) {
            sb.append(String.format(Locale.UK, "%-20s %2d x %8s = %8s%n",
                    line.getDescription(),
                    line.getQuantity(),
                    formatEuro(line.getUnitPrice()),
                    formatEuro(line.lineTotal())));
        }

        sb.append("-".repeat(40)).append("\n");
        sb.append(String.format(Locale.UK, "%-30s %8s%n", "Subtotal (excl. VAT):", formatEuro(subtotal)));
        if (discount.compareTo(BigDecimal.ZERO) > 0) {
            sb.append(String.format(Locale.UK, "%-30s -%7s%n", "Discount (10%):", formatEuro(discount)));
            sb.append(String.format(Locale.UK, "%-30s %8s%n", "Subtotal after discount:", formatEuro(discountedSubtotal)));
        }
        sb.append(String.format(Locale.UK, "%-30s %8s%n", "VAT (21%):", formatEuro(vat)));
        sb.append(String.format(Locale.UK, "%-30s %8s%n", "Total:", formatEuro(total)));

        return sb.toString();
    }

    private String formatEuro(BigDecimal amount) {
        return "EUR " + amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    public static void main(String[] args) {
        List<OrderLine> lines = new ArrayList<>();
        lines.add(new OrderLine("Lottery subscription - annual", 1, new BigDecimal("85.00")));
        lines.add(new OrderLine("Charity add-on", 2, new BigDecimal("10.00")));

        OrderProcessor processor = new OrderProcessor();
        System.out.println(processor.processOrder("ORD-0001", lines));
    }
}
