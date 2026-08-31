import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

/**
 * Processes an order: validates line items, applies a 10% discount when the
 * pre-VAT subtotal exceeds EUR 100.00, adds 21% VAT, and renders a receipt.
 */
public final class OrderProcessor {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    /** A single order line: item name, quantity, and unit price excluding VAT. */
    public record LineItem(String name, int quantity, BigDecimal unitPrice) {

        public LineItem {
            Objects.requireNonNull(unitPrice, "unitPrice must not be null");
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Line item name must not be blank");
            }
            if (quantity <= 0) {
                throw new IllegalArgumentException(
                        "Quantity must be positive for item '" + name + "', got: " + quantity);
            }
            if (unitPrice.signum() < 0) {
                throw new IllegalArgumentException(
                        "Unit price must not be negative for item '" + name + "', got: " + unitPrice);
            }
        }

        public BigDecimal lineTotal() {
            return unitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(SCALE, ROUNDING);
        }
    }

    /** Computed totals for an order. All amounts in EUR, rounded to 2 decimal places. */
    public record OrderTotals(
            BigDecimal subtotal,
            BigDecimal discount,
            BigDecimal netAfterDiscount,
            BigDecimal vat,
            BigDecimal total) {
    }

    private OrderProcessor() {
    }

    /** Validates the order and computes all totals. */
    public static OrderTotals computeTotals(List<LineItem> items) {
        validate(items);

        BigDecimal subtotal = items.stream()
                .map(LineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(SCALE, ROUNDING);

        BigDecimal discount = subtotal.compareTo(DISCOUNT_THRESHOLD) > 0
                ? subtotal.multiply(DISCOUNT_RATE).setScale(SCALE, ROUNDING)
                : BigDecimal.ZERO.setScale(SCALE, ROUNDING);

        BigDecimal net = subtotal.subtract(discount);
        BigDecimal vat = net.multiply(VAT_RATE).setScale(SCALE, ROUNDING);
        BigDecimal total = net.add(vat);

        return new OrderTotals(subtotal, discount, net, vat, total);
    }

    private static void validate(List<LineItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one line item");
        }
        // Record constructor enforces per-item rules; reject null entries here.
        if (items.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Order must not contain null line items");
        }
    }

    /** Produces a plain-text receipt for the given order. */
    public static String buildReceipt(String orderId, List<LineItem> items) {
        OrderTotals totals = computeTotals(items);
        StringBuilder sb = new StringBuilder();

        sb.append("RECEIPT").append(System.lineSeparator());
        sb.append("Order: ").append(orderId).append(System.lineSeparator());
        sb.append("----------------------------------------").append(System.lineSeparator());

        for (LineItem item : items) {
            sb.append(String.format("%-20s %3d x %8s = %9s%n",
                    truncate(item.name(), 20),
                    item.quantity(),
                    "EUR " + item.unitPrice().setScale(SCALE, ROUNDING),
                    "EUR " + item.lineTotal()));
        }

        sb.append("----------------------------------------").append(System.lineSeparator());
        appendRow(sb, "Subtotal (excl. VAT)", totals.subtotal());
        if (totals.discount().signum() > 0) {
            appendRow(sb, "Discount (10%)", totals.discount().negate());
            appendRow(sb, "Net after discount", totals.netAfterDiscount());
        }
        appendRow(sb, "VAT (21%)", totals.vat());
        sb.append("----------------------------------------").append(System.lineSeparator());
        appendRow(sb, "TOTAL", totals.total());

        return sb.toString();
    }

    private static void appendRow(StringBuilder sb, String label, BigDecimal amount) {
        sb.append(String.format("%-26s EUR %9s%n", label, amount.toPlainString()));
    }

    private static String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + ".";
    }

    public static void main(String[] args) {
        List<LineItem> order = List.of(
                new LineItem("Notebook", 3, new BigDecimal("12.50")),
                new LineItem("Desk lamp", 1, new BigDecimal("45.00")),
                new LineItem("USB-C cable", 2, new BigDecimal("14.75"))
        );

        System.out.println(buildReceipt("ORD-2026-0001", order));
    }
}
