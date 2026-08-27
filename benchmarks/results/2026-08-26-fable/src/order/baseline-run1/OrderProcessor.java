import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public final class OrderProcessor {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");

    public record LineItem(String name, int quantity, BigDecimal unitPrice) {}

    public static class OrderValidationException extends RuntimeException {
        public OrderValidationException(String message) {
            super(message);
        }
    }

    /**
     * Validates the order, computes totals and returns a formatted receipt.
     *
     * @throws OrderValidationException if the order or any line item is invalid
     */
    public static String processOrder(List<LineItem> items) {
        validate(items);

        BigDecimal subtotal = items.stream()
                .map(item -> item.unitPrice().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal discount = BigDecimal.ZERO.setScale(2);
        if (subtotal.compareTo(DISCOUNT_THRESHOLD) > 0) {
            discount = subtotal.multiply(DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal netAfterDiscount = subtotal.subtract(discount);
        BigDecimal vat = netAfterDiscount.multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = netAfterDiscount.add(vat);

        return buildReceipt(items, subtotal, discount, netAfterDiscount, vat, total);
    }

    private static void validate(List<LineItem> items) {
        if (items == null || items.isEmpty()) {
            throw new OrderValidationException("Order must contain at least one line item");
        }
        for (int i = 0; i < items.size(); i++) {
            LineItem item = items.get(i);
            if (item == null) {
                throw new OrderValidationException("Line item " + (i + 1) + " is null");
            }
            if (item.name() == null || item.name().isBlank()) {
                throw new OrderValidationException("Line item " + (i + 1) + " has no name");
            }
            if (item.quantity() <= 0) {
                throw new OrderValidationException(
                        "Line item '" + item.name() + "' has a non-positive quantity: " + item.quantity());
            }
            if (item.unitPrice() == null || item.unitPrice().signum() < 0) {
                throw new OrderValidationException(
                        "Line item '" + item.name() + "' has a missing or negative unit price");
            }
        }
    }

    private static String buildReceipt(List<LineItem> items, BigDecimal subtotal, BigDecimal discount,
                                       BigDecimal netAfterDiscount, BigDecimal vat, BigDecimal total) {
        StringBuilder sb = new StringBuilder();
        sb.append("RECEIPT\n");
        sb.append("--------------------------------------------\n");
        for (LineItem item : items) {
            BigDecimal lineTotal = item.unitPrice()
                    .multiply(BigDecimal.valueOf(item.quantity()))
                    .setScale(2, RoundingMode.HALF_UP);
            sb.append(String.format("%-20s %3d x %8s  %9s%n",
                    truncate(item.name(), 20), item.quantity(),
                    format(item.unitPrice().setScale(2, RoundingMode.HALF_UP)), format(lineTotal)));
        }
        sb.append("--------------------------------------------\n");
        sb.append(String.format("%-34s %9s%n", "Subtotal (excl. VAT):", format(subtotal)));
        if (discount.signum() > 0) {
            sb.append(String.format("%-34s %9s%n", "Discount (10%):", "-" + format(discount)));
            sb.append(String.format("%-34s %9s%n", "Net after discount:", format(netAfterDiscount)));
        }
        sb.append(String.format("%-34s %9s%n", "VAT (21%):", format(vat)));
        sb.append("--------------------------------------------\n");
        sb.append(String.format("%-34s %9s%n", "TOTAL:", format(total)));
        return sb.toString();
    }

    private static String format(BigDecimal amount) {
        return "EUR " + amount.toPlainString();
    }

    private static String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + ".";
    }

    public static void main(String[] args) {
        List<LineItem> order = List.of(
                new LineItem("Notebook", 3, new BigDecimal("12.50")),
                new LineItem("Desk lamp", 2, new BigDecimal("34.95")),
                new LineItem("USB-C cable", 1, new BigDecimal("9.99"))
        );
        System.out.println(processOrder(order));
    }
}
