import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

public final class OrderProcessor {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");

    public record LineItem(String description, int quantity, BigDecimal unitPrice) {

        public LineItem {
            Objects.requireNonNull(description, "description must not be null");
            if (description.isBlank()) {
                throw new IllegalArgumentException("description must not be blank");
            }
            if (quantity <= 0) {
                throw new IllegalArgumentException("quantity must be positive: " + quantity);
            }
            Objects.requireNonNull(unitPrice, "unitPrice must not be null");
            if (unitPrice.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("unitPrice must not be negative: " + unitPrice);
            }
        }

        BigDecimal lineTotal() {
            return unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    public record Receipt(
            BigDecimal subtotal,
            BigDecimal discount,
            BigDecimal vat,
            BigDecimal total
    ) {
    }

    public Receipt process(List<LineItem> lineItems) {
        if (lineItems == null || lineItems.isEmpty()) {
            throw new IllegalArgumentException("order must contain at least one line item");
        }

        BigDecimal subtotal = lineItems.stream()
                .map(LineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        boolean discountApplies = subtotal.compareTo(DISCOUNT_THRESHOLD) > 0;
        BigDecimal discount = discountApplies
                ? subtotal.multiply(DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal discountedSubtotal = subtotal.subtract(discount);
        BigDecimal vat = discountedSubtotal.multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = discountedSubtotal.add(vat).setScale(2, RoundingMode.HALF_UP);

        return new Receipt(subtotal, discount, vat, total);
    }

    public String toReceiptString(List<LineItem> lineItems) {
        Receipt receipt = process(lineItems);

        StringBuilder sb = new StringBuilder();
        sb.append("Order Receipt\n");
        sb.append("-------------\n");
        for (LineItem item : lineItems) {
            sb.append(String.format(
                    "%-20s %3d x %8s = %10s%n",
                    item.description(),
                    item.quantity(),
                    formatEuro(item.unitPrice()),
                    formatEuro(item.lineTotal().setScale(2, RoundingMode.HALF_UP))
            ));
        }
        sb.append("-------------\n");
        sb.append(String.format("%-20s %19s%n", "Subtotal (excl. VAT):", formatEuro(receipt.subtotal())));
        sb.append(String.format("%-20s %19s%n", "Discount (10%):", formatEuro(receipt.discount())));
        sb.append(String.format("%-20s %19s%n", "VAT (21%):", formatEuro(receipt.vat())));
        sb.append(String.format("%-20s %19s%n", "Total:", formatEuro(receipt.total())));
        return sb.toString();
    }

    private static String formatEuro(BigDecimal amount) {
        return "€" + amount.setScale(2, RoundingMode.HALF_UP);
    }

    public static void main(String[] args) {
        OrderProcessor processor = new OrderProcessor();

        List<LineItem> lineItems = List.of(
                new LineItem("Widget", 3, new BigDecimal("15.00")),
                new LineItem("Gadget", 2, new BigDecimal("30.00"))
        );

        System.out.print(processor.toReceiptString(lineItems));
    }
}
