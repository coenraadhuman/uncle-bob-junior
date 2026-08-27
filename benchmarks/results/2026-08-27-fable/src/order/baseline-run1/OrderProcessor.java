import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Processes an order: validates line items, applies a 10% discount when the
 * pre-VAT subtotal exceeds 100.00 EUR, adds 21% VAT on the discounted amount,
 * and produces a receipt string.
 */
public final class OrderProcessor {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    /** A single order line: product name, quantity, and unit price excluding VAT. */
    public record LineItem(String name, int quantity, BigDecimal unitPrice) { }

    /** Computed totals plus the formatted receipt. */
    public record Receipt(BigDecimal subtotal, BigDecimal discount, BigDecimal vat,
                          BigDecimal total, String text) { }

    private OrderProcessor() { }

    public static Receipt processOrder(List<LineItem> items) {
        validate(items);

        BigDecimal subtotal = items.stream()
                .map(item -> item.unitPrice().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(SCALE, ROUNDING);

        BigDecimal discount = subtotal.compareTo(DISCOUNT_THRESHOLD) > 0
                ? subtotal.multiply(DISCOUNT_RATE).setScale(SCALE, ROUNDING)
                : BigDecimal.ZERO.setScale(SCALE, ROUNDING);

        BigDecimal discountedSubtotal = subtotal.subtract(discount);
        BigDecimal vat = discountedSubtotal.multiply(VAT_RATE).setScale(SCALE, ROUNDING);
        BigDecimal total = discountedSubtotal.add(vat);

        return new Receipt(subtotal, discount, vat, total,
                buildReceiptText(items, subtotal, discount, vat, total));
    }

    private static void validate(List<LineItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one line item");
        }
        for (int i = 0; i < items.size(); i++) {
            LineItem item = items.get(i);
            if (item == null) {
                throw new IllegalArgumentException("Line item " + (i + 1) + " is null");
            }
            if (item.name() == null || item.name().isBlank()) {
                throw new IllegalArgumentException("Line item " + (i + 1) + " has no name");
            }
            if (item.quantity() <= 0) {
                throw new IllegalArgumentException(
                        "Line item '" + item.name() + "' has non-positive quantity: " + item.quantity());
            }
            if (item.unitPrice() == null || item.unitPrice().signum() < 0) {
                throw new IllegalArgumentException(
                        "Line item '" + item.name() + "' has a missing or negative unit price");
            }
        }
    }

    private static String buildReceiptText(List<LineItem> items, BigDecimal subtotal,
                                           BigDecimal discount, BigDecimal vat, BigDecimal total) {
        StringBuilder sb = new StringBuilder();
        sb.append("RECEIPT\n");
        sb.append("--------------------------------------------\n");
        for (LineItem item : items) {
            BigDecimal lineTotal = item.unitPrice()
                    .multiply(BigDecimal.valueOf(item.quantity()))
                    .setScale(SCALE, ROUNDING);
            sb.append(String.format("%-20s %3d x %8s EUR %10s%n",
                    item.name(), item.quantity(),
                    item.unitPrice().setScale(SCALE, ROUNDING), lineTotal));
        }
        sb.append("--------------------------------------------\n");
        sb.append(String.format("%-33s EUR %10s%n", "Subtotal (excl. VAT)", subtotal));
        if (discount.signum() > 0) {
            sb.append(String.format("%-33s EUR %10s%n", "Discount (10%)", discount.negate()));
        }
        sb.append(String.format("%-33s EUR %10s%n", "VAT (21%)", vat));
        sb.append("--------------------------------------------\n");
        sb.append(String.format("%-33s EUR %10s%n", "TOTAL", total));
        return sb.toString();
    }

    public static void main(String[] args) {
        List<LineItem> order = List.of(
                new LineItem("Notebook", 3, new BigDecimal("12.50")),
                new LineItem("Desk lamp", 2, new BigDecimal("34.95")));

        Receipt receipt = processOrder(order);
        System.out.print(receipt.text());
    }
}
