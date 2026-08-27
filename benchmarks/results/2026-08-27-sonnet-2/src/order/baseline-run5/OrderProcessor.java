import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;

public final class OrderProcessor {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final int SCALE = 2;

    public record LineItem(String description, int quantity, BigDecimal unitPrice) {

        public LineItem {
            if (description == null || description.isBlank()) {
                throw new InvalidOrderException("Line item description must not be blank");
            }
            if (quantity <= 0) {
                throw new InvalidOrderException("Line item quantity must be positive: " + description);
            }
            if (unitPrice == null || unitPrice.signum() < 0) {
                throw new InvalidOrderException("Line item unit price must be non-negative: " + description);
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
            BigDecimal total,
            String text) {
    }

    public static Receipt processOrder(List<LineItem> lineItems) {
        if (lineItems == null || lineItems.isEmpty()) {
            throw new InvalidOrderException("Order must contain at least one line item");
        }

        BigDecimal subtotal = lineItems.stream()
                .map(LineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(SCALE, RoundingMode.HALF_UP);

        BigDecimal discount = subtotal.compareTo(DISCOUNT_THRESHOLD) > 0
                ? subtotal.multiply(DISCOUNT_RATE).setScale(SCALE, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(SCALE);

        BigDecimal discountedSubtotal = subtotal.subtract(discount);
        BigDecimal vat = discountedSubtotal.multiply(VAT_RATE).setScale(SCALE, RoundingMode.HALF_UP);
        BigDecimal total = discountedSubtotal.add(vat).setScale(SCALE, RoundingMode.HALF_UP);

        String text = buildReceiptText(lineItems, subtotal, discount, vat, total);

        return new Receipt(subtotal, discount, vat, total, text);
    }

    private static String buildReceiptText(
            List<LineItem> lineItems,
            BigDecimal subtotal,
            BigDecimal discount,
            BigDecimal vat,
            BigDecimal total) {

        StringBuilder sb = new StringBuilder();
        sb.append("Order Receipt\n");
        sb.append("-------------\n");

        for (LineItem item : lineItems) {
            sb.append(String.format(Locale.UK, "%-25s %2d x %8.2f = %8.2f%n",
                    item.description(), item.quantity(), item.unitPrice(), item.lineTotal()));
        }

        sb.append("-------------\n");
        sb.append(String.format(Locale.UK, "%-25s %19.2f%n", "Subtotal (excl. VAT):", subtotal));
        if (discount.signum() > 0) {
            sb.append(String.format(Locale.UK, "%-25s %19.2f%n", "Discount (10%):", discount.negate()));
        }
        sb.append(String.format(Locale.UK, "%-25s %19.2f%n", "VAT (21%):", vat));
        sb.append(String.format(Locale.UK, "%-25s %19.2f%n", "Total (EUR):", total));

        return sb.toString();
    }

    public static class InvalidOrderException extends RuntimeException {
        public InvalidOrderException(String message) {
            super(message);
        }
    }

    public static void main(String[] args) {
        List<LineItem> items = List.of(
                new LineItem("Charity lottery ticket - monthly", 2, new BigDecimal("29.95")),
                new LineItem("Charity lottery ticket - extra", 1, new BigDecimal("49.95"))
        );

        Receipt receipt = processOrder(items);
        System.out.println(receipt.text());
    }
}
