import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;

/** A single order line: a product name, a quantity, and a unit price in EUR (excl. VAT). */
record LineItem(String name, int quantity, BigDecimal unitPrice) {

    LineItem {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Line item name must not be blank");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Line item '" + name + "': quantity must be positive, was " + quantity);
        }
        if (unitPrice == null || unitPrice.signum() < 0) {
            throw new IllegalArgumentException(
                    "Line item '" + name + "': unit price must be non-negative, was " + unitPrice);
        }
    }

    BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}

/** Processes an order: validates items, applies discount and VAT, and renders a receipt. */
public final class OrderProcessor {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    public String processOrder(List<LineItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one line item");
        }

        BigDecimal subtotal = items.stream()
                .map(LineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(SCALE, ROUNDING);

        boolean discountApplies = subtotal.compareTo(DISCOUNT_THRESHOLD) > 0;
        BigDecimal discount = discountApplies
                ? subtotal.multiply(DISCOUNT_RATE).setScale(SCALE, ROUNDING)
                : BigDecimal.ZERO.setScale(SCALE, ROUNDING);

        BigDecimal netAfterDiscount = subtotal.subtract(discount);
        BigDecimal vat = netAfterDiscount.multiply(VAT_RATE).setScale(SCALE, ROUNDING);
        BigDecimal total = netAfterDiscount.add(vat);

        return buildReceipt(items, subtotal, discount, netAfterDiscount, vat, total, discountApplies);
    }

    private String buildReceipt(List<LineItem> items,
                                BigDecimal subtotal,
                                BigDecimal discount,
                                BigDecimal net,
                                BigDecimal vat,
                                BigDecimal total,
                                boolean discountApplies) {
        StringBuilder sb = new StringBuilder();
        sb.append("RECEIPT\n");
        sb.append("--------------------------------------------\n");
        for (LineItem item : items) {
            sb.append(String.format(Locale.ROOT, "%-20s %3d x %8s = %10s%n",
                    item.name(), item.quantity(),
                    money(item.unitPrice().setScale(SCALE, ROUNDING)),
                    money(item.lineTotal().setScale(SCALE, ROUNDING))));
        }
        sb.append("--------------------------------------------\n");
        sb.append(String.format(Locale.ROOT, "%-30s %13s%n", "Subtotal (excl. VAT)", money(subtotal)));
        if (discountApplies) {
            sb.append(String.format(Locale.ROOT, "%-30s %13s%n", "Discount (10%)", "-" + money(discount)));
            sb.append(String.format(Locale.ROOT, "%-30s %13s%n", "Net after discount", money(net)));
        }
        sb.append(String.format(Locale.ROOT, "%-30s %13s%n", "VAT (21%)", money(vat)));
        sb.append("--------------------------------------------\n");
        sb.append(String.format(Locale.ROOT, "%-30s %13s%n", "TOTAL", money(total)));
        return sb.toString();
    }

    private static String money(BigDecimal amount) {
        return "EUR " + amount.toPlainString();
    }

    public static void main(String[] args) {
        OrderProcessor processor = new OrderProcessor();
        List<LineItem> order = List.of(
                new LineItem("Widget", 3, new BigDecimal("25.00")),
                new LineItem("Gadget", 2, new BigDecimal("19.95"))
        );
        System.out.println(processor.processOrder(order));
    }
}
