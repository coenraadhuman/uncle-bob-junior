// OrderReceipt.java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public final class OrderReceipt {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final int MONEY_SCALE = 2;

    private OrderReceipt() {
    }

    public static String process(List<LineItem> lineItems) {
        List<LineItem> validItems = requireNonEmpty(lineItems);
        BigDecimal subtotal = subtotalOf(validItems);
        BigDecimal discount = discountFor(subtotal);
        BigDecimal discountedSubtotal = subtotal.subtract(discount);
        BigDecimal vat = vatFor(discountedSubtotal);
        BigDecimal total = discountedSubtotal.add(vat);
        return buildReceipt(validItems, subtotal, discount, vat, total);
    }

    private static List<LineItem> requireNonEmpty(List<LineItem> lineItems) {
        if (lineItems == null || lineItems.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one line item");
        }
        return lineItems;
    }

    private static BigDecimal subtotalOf(List<LineItem> lineItems) {
        return lineItems.stream()
                .map(LineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal discountFor(BigDecimal subtotal) {
        if (subtotal.compareTo(DISCOUNT_THRESHOLD) <= 0) {
            return BigDecimal.ZERO;
        }
        return round(subtotal.multiply(DISCOUNT_RATE));
    }

    private static BigDecimal vatFor(BigDecimal amount) {
        return round(amount.multiply(VAT_RATE));
    }

    private static BigDecimal round(BigDecimal amount) {
        return amount.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private static String buildReceipt(List<LineItem> lineItems, BigDecimal subtotal,
                                        BigDecimal discount, BigDecimal vat, BigDecimal total) {
        StringBuilder receipt = new StringBuilder();
        receipt.append("Receipt\n-------\n");
        lineItems.forEach(item -> receipt.append(formatLine(item)));
        receipt.append("-------\n");
        receipt.append(formatTotal("Subtotal", subtotal));
        if (discount.signum() > 0) {
            receipt.append(formatTotal("Discount (10%)", discount.negate()));
        }
        receipt.append(formatTotal("VAT (21%)", vat));
        receipt.append(formatTotal("Total", total));
        return receipt.toString();
    }

    private static String formatLine(LineItem item) {
        return String.format("%2dx %-20s EUR %8.2f%n",
                item.quantity(), item.description(), item.lineTotal());
    }

    private static String formatTotal(String label, BigDecimal amount) {
        return String.format("%-16s EUR %8.2f%n", label, amount);
    }
}
