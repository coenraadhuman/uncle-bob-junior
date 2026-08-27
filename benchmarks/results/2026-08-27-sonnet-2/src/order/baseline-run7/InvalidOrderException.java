import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/** Thrown when an order or one of its line items fails validation. */
final class InvalidOrderException extends RuntimeException {
    InvalidOrderException(String message) {
        super(message);
    }
}

/** A single line item on an order. */
final class LineItem {

    private final String description;
    private final int quantity;
    private final BigDecimal unitPrice;

    LineItem(String description, int quantity, BigDecimal unitPrice) {
        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    String getDescription() {
        return description;
    }

    int getQuantity() {
        return quantity;
    }

    BigDecimal getUnitPrice() {
        return unitPrice;
    }

    BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}

/** An order consisting of one or more line items. */
final class Order {

    private final String orderId;
    private final List<LineItem> lineItems;

    Order(String orderId, List<LineItem> lineItems) {
        this.orderId = orderId;
        this.lineItems = lineItems;
    }

    String getOrderId() {
        return orderId;
    }

    List<LineItem> getLineItems() {
        return lineItems;
    }
}

/** Validates orders, applies discount/VAT rules, and renders a receipt. */
final class OrderProcessor {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final DateTimeFormatter RECEIPT_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");

    /**
     * Validates the order, computes discount and VAT, and returns a formatted receipt.
     *
     * @throws InvalidOrderException if the order or any line item is invalid
     */
    String processOrder(Order order, LocalDate receiptDate) {
        validate(order);

        BigDecimal subtotal = order.getLineItems().stream()
                .map(LineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        boolean discountApplies = subtotal.compareTo(DISCOUNT_THRESHOLD) > 0;
        BigDecimal discount = discountApplies
                ? subtotal.multiply(DISCOUNT_RATE)
                : BigDecimal.ZERO;

        BigDecimal discountedSubtotal = subtotal.subtract(discount);
        BigDecimal vat = discountedSubtotal.multiply(VAT_RATE);
        BigDecimal total = discountedSubtotal.add(vat);

        return buildReceipt(order, subtotal, discount, discountedSubtotal, vat, total, receiptDate);
    }

    private void validate(Order order) {
        if (order == null) {
            throw new InvalidOrderException("Order must not be null.");
        }
        if (order.getOrderId() == null || order.getOrderId().isBlank()) {
            throw new InvalidOrderException("Order must have a non-blank order ID.");
        }
        if (order.getLineItems() == null || order.getLineItems().isEmpty()) {
            throw new InvalidOrderException("Order " + order.getOrderId() + " must contain at least one line item.");
        }
        for (LineItem item : order.getLineItems()) {
            validateLineItem(order.getOrderId(), item);
        }
    }

    private void validateLineItem(String orderId, LineItem item) {
        if (item == null) {
            throw new InvalidOrderException("Order " + orderId + " contains a null line item.");
        }
        if (item.getDescription() == null || item.getDescription().isBlank()) {
            throw new InvalidOrderException("Order " + orderId + " has a line item with a blank description.");
        }
        if (item.getQuantity() < 1) {
            throw new InvalidOrderException(
                    "Order " + orderId + ": quantity for '" + item.getDescription() + "' must be at least 1.");
        }
        if (item.getUnitPrice() == null || item.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidOrderException(
                    "Order " + orderId + ": unit price for '" + item.getDescription() + "' must not be negative.");
        }
    }

    private String buildReceipt(
            Order order,
            BigDecimal subtotal,
            BigDecimal discount,
            BigDecimal discountedSubtotal,
            BigDecimal vat,
            BigDecimal total,
            LocalDate receiptDate) {

        StringBuilder sb = new StringBuilder();
        sb.append("Receipt for order ").append(order.getOrderId()).append('\n');
        sb.append("Date: ").append(receiptDate.format(RECEIPT_DATE_FORMAT)).append('\n');
        sb.append("--------------------------------------------------\n");

        for (LineItem item : order.getLineItems()) {
            sb.append(String.format(
                    "%-25s %3d x %8s = %10s%n",
                    item.getDescription(),
                    item.getQuantity(),
                    formatAmount(item.getUnitPrice()),
                    formatAmount(item.lineTotal())));
        }

        sb.append("--------------------------------------------------\n");
        sb.append(String.format("%-38s %10s%n", "Subtotal (excl. VAT):", formatAmount(subtotal)));
        if (discount.compareTo(BigDecimal.ZERO) > 0) {
            sb.append(String.format("%-38s -%9s%n", "Discount (10%):", formatAmount(discount)));
            sb.append(String.format("%-38s %10s%n", "Subtotal after discount:", formatAmount(discountedSubtotal)));
        }
        sb.append(String.format("%-38s %10s%n", "VAT (21%):", formatAmount(vat)));
        sb.append("--------------------------------------------------\n");
        sb.append(String.format("%-38s %10s%n", "Total:", formatAmount(total)));

        return sb.toString();
    }

    private String formatAmount(BigDecimal amount) {
        return "\u20ac" + amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}

/** Example usage. */
final class OrderProcessingDemo {

    public static void main(String[] args) {
        Order order = new Order(
                "ORD-1001",
                List.of(
                        new LineItem("Lottery subscription - monthly", 2, new BigDecimal("45.00")),
                        new LineItem("Charity add-on", 1, new BigDecimal("15.50"))
                ));

        OrderProcessor processor = new OrderProcessor();
        String receipt = processor.processOrder(order, LocalDate.of(2026, 8, 27));
        System.out.println(receipt);
    }
}
