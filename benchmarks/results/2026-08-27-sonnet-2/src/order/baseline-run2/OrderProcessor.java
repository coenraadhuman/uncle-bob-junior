import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class OrderProcessor {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final NumberFormat CURRENCY_FORMAT =
            NumberFormat.getCurrencyInstance(new Locale("nl", "NL"));

    public record LineItem(String description, int quantity, BigDecimal unitPrice) {

        BigDecimal lineTotal() {
            return unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    public record Receipt(
            String text,
            BigDecimal subtotal,
            BigDecimal discount,
            BigDecimal vat,
            BigDecimal total) {
    }

    public static void validate(List<LineItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one line item");
        }
        for (LineItem item : items) {
            if (item == null) {
                throw new IllegalArgumentException("Line item cannot be null");
            }
            if (item.description() == null || item.description().isBlank()) {
                throw new IllegalArgumentException("Line item description cannot be blank");
            }
            if (item.quantity() <= 0) {
                throw new IllegalArgumentException(
                        "Line item '" + item.description() + "' must have a positive quantity");
            }
            if (item.unitPrice() == null || item.unitPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException(
                        "Line item '" + item.description() + "' must have a non-negative unit price");
            }
        }
    }

    public static Receipt process(List<LineItem> items) {
        validate(items);

        BigDecimal subtotal = items.stream()
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

        String text = buildReceiptText(items, subtotal, discount, vat, total, discountApplies);

        return new Receipt(text, subtotal, discount, vat, total);
    }

    private static String buildReceiptText(
            List<LineItem> items,
            BigDecimal subtotal,
            BigDecimal discount,
            BigDecimal vat,
            BigDecimal total,
            boolean discountApplies) {

        StringBuilder sb = new StringBuilder();
        sb.append("=== Receipt ===\n");
        for (LineItem item : items) {
            sb.append(String.format(
                    "%-20s %3d x %10s = %12s%n",
                    item.description(),
                    item.quantity(),
                    CURRENCY_FORMAT.format(item.unitPrice()),
                    CURRENCY_FORMAT.format(item.lineTotal())));
        }
        sb.append("----------------------------------------\n");
        sb.append(String.format("%-30s %12s%n", "Subtotal (excl. VAT):", CURRENCY_FORMAT.format(subtotal)));
        if (discountApplies) {
            sb.append(String.format("%-30s %12s%n", "Discount (10%):", "-" + CURRENCY_FORMAT.format(discount)));
        }
        sb.append(String.format("%-30s %12s%n", "VAT (21%):", CURRENCY_FORMAT.format(vat)));
        sb.append(String.format("%-30s %12s%n", "Total (incl. VAT):", CURRENCY_FORMAT.format(total)));
        return sb.toString();
    }

    public static void main(String[] args) {
        List<LineItem> order = List.of(
                new LineItem("Lottery ticket - monthly subscription", 2, new BigDecimal("45.00")),
                new LineItem("Donation add-on", 1, new BigDecimal("15.00"))
        );

        Receipt receipt = process(order);
        System.out.println(receipt.text());
    }
}
