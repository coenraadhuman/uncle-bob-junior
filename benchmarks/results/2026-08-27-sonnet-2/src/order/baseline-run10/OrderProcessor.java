import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class OrderProcessor {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");

    public static final class LineItem {
        private final String description;
        private final int quantity;
        private final BigDecimal unitPrice;

        public LineItem(String description, int quantity, BigDecimal unitPrice) {
            this.description = description;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }

        public String getDescription() { return description; }
        public int getQuantity() { return quantity; }
        public BigDecimal getUnitPrice() { return unitPrice; }

        public BigDecimal lineTotal() {
            return unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    public static final class Order {
        private final String orderId;
        private final List<LineItem> lineItems;

        public Order(String orderId, List<LineItem> lineItems) {
            this.orderId = orderId;
            this.lineItems = lineItems;
        }

        public String getOrderId() { return orderId; }
        public List<LineItem> getLineItems() { return lineItems; }
    }

    public static final class OrderTotals {
        public final BigDecimal subtotal;
        public final BigDecimal discount;
        public final BigDecimal vat;
        public final BigDecimal grandTotal;

        public OrderTotals(BigDecimal subtotal, BigDecimal discount, BigDecimal vat, BigDecimal grandTotal) {
            this.subtotal = subtotal;
            this.discount = discount;
            this.vat = vat;
            this.grandTotal = grandTotal;
        }
    }

    public static class InvalidOrderException extends RuntimeException {
        public InvalidOrderException(String message) {
            super(message);
        }
    }

    public void validateLineItems(Order order) {
        if (order.getLineItems() == null || order.getLineItems().isEmpty()) {
            throw new InvalidOrderException("Order must contain at least one line item.");
        }
        for (LineItem item : order.getLineItems()) {
            if (item.getDescription() == null || item.getDescription().isBlank()) {
                throw new InvalidOrderException("Line item description must not be blank.");
            }
            if (item.getQuantity() <= 0) {
                throw new InvalidOrderException(
                        "Line item \"" + item.getDescription() + "\" must have a positive quantity.");
            }
            if (item.getUnitPrice() == null || item.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new InvalidOrderException(
                        "Line item \"" + item.getDescription() + "\" must have a non-negative unit price.");
            }
        }
    }

    public OrderTotals computeTotals(Order order) {
        BigDecimal subtotal = order.getLineItems().stream()
                .map(LineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal discount = subtotal.compareTo(DISCOUNT_THRESHOLD) > 0
                ? subtotal.multiply(DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal discountedSubtotal = subtotal.subtract(discount);
        BigDecimal vat = discountedSubtotal.multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal grandTotal = discountedSubtotal.add(vat);

        return new OrderTotals(subtotal, discount, vat, grandTotal);
    }

    public String buildReceipt(Order order, OrderTotals totals) {
        StringBuilder sb = new StringBuilder();
        sb.append("Receipt for order ").append(order.getOrderId()).append('\n');
        sb.append("-".repeat(40)).append('\n');

        for (LineItem item : order.getLineItems()) {
            sb.append(String.format(Locale.ROOT, "%-20s %2d x %8s = %8s%n",
                    item.getDescription(),
                    item.getQuantity(),
                    formatEuro(item.getUnitPrice()),
                    formatEuro(item.lineTotal())));
        }

        sb.append("-".repeat(40)).append('\n');
        sb.append(String.format(Locale.ROOT, "%-30s %8s%n", "Subtotal (excl. VAT):", formatEuro(totals.subtotal)));
        if (totals.discount.compareTo(BigDecimal.ZERO) > 0) {
            sb.append(String.format(Locale.ROOT, "%-30s -%8s%n", "Discount (10%):", formatEuro(totals.discount)));
        }
        sb.append(String.format(Locale.ROOT, "%-30s %8s%n", "VAT (21%):", formatEuro(totals.vat)));
        sb.append(String.format(Locale.ROOT, "%-30s %8s%n", "Total (incl. VAT):", formatEuro(totals.grandTotal)));

        return sb.toString();
    }

    public String processOrder(Order order) {
        validateLineItems(order);
        OrderTotals totals = computeTotals(order);
        return buildReceipt(order, totals);
    }

    private static String formatEuro(BigDecimal amount) {
        return "EUR " + amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    public static void main(String[] args) {
        Order order = new Order("ORD-1001", List.of(
                new LineItem("Charity draw ticket", 3, new BigDecimal("25.00")),
                new LineItem("Processing fee", 1, new BigDecimal("30.00"))
        ));

        OrderProcessor processor = new OrderProcessor();
        System.out.println(processor.processOrder(order));
    }
}
