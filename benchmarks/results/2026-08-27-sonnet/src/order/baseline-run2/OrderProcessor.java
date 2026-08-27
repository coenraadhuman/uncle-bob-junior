import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class OrderProcessor {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");

    public record LineItem(String description, int quantity, BigDecimal unitPrice) {

        public BigDecimal lineTotal() {
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

    /**
     * Validates line items, throwing if any are malformed.
     */
    public static void validate(List<LineItem> lineItems) {
        if (lineItems == null || lineItems.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one line item.");
        }
        for (LineItem item : lineItems) {
            if (item.description() == null || item.description().isBlank()) {
                throw new IllegalArgumentException("Line item description must not be blank.");
            }
            if (item.quantity() <= 0) {
                throw new IllegalArgumentException(
                        "Line item '" + item.description() + "' must have a positive quantity.");
            }
            if (item.unitPrice() == null || item.unitPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException(
                        "Line item '" + item.description() + "' must have a non-negative unit price.");
            }
        }
    }

    /**
     * Validates the order, applies the volume discount and VAT, and builds a receipt.
     */
    public static Receipt process(List<LineItem> lineItems) {
        validate(lineItems);

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
        BigDecimal total = discountedSubtotal.add(vat);

        String text = buildReceiptText(lineItems, subtotal, discount, vat, total, discountApplies);

        return new Receipt(subtotal, discount, vat, total, text);
    }

    private static String buildReceiptText(
            List<LineItem> lineItems,
            BigDecimal subtotal,
            BigDecimal discount,
            BigDecimal vat,
            BigDecimal total,
            boolean discountApplies) {

        StringBuilder sb = new StringBuilder();
        sb.append("Receipt\n");
        sb.append("-------\n");
        for (LineItem item : lineItems) {
            sb.append(String.format(Locale.UK, "%-20s %2d x EUR %8.2f = EUR %8.2f%n",
                    item.description(), item.quantity(), item.unitPrice(), item.lineTotal()));
        }
        sb.append("-------\n");
        sb.append(String.format(Locale.UK, "Subtotal (excl. VAT): EUR %8.2f%n", subtotal));
        if (discountApplies) {
            sb.append(String.format(Locale.UK, "Discount (10%%):       EUR %8.2f%n", discount.negate()));
        }
        sb.append(String.format(Locale.UK, "VAT (21%%):            EUR %8.2f%n", vat));
        sb.append(String.format(Locale.UK, "Total:                EUR %8.2f%n", total));
        return sb.toString();
    }

    public static void main(String[] args) {
        List<LineItem> order = new ArrayList<>();
        order.add(new LineItem("Lottery ticket bundle", 3, new BigDecimal("25.00")));
        order.add(new LineItem("Charity add-on", 2, new BigDecimal("15.00")));

        Receipt receipt = process(order);
        System.out.print(receipt.text());
    }
}
