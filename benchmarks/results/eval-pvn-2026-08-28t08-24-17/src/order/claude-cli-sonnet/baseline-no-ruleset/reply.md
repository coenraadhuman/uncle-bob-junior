Assumptions: prices are in EUR using `BigDecimal` for accuracy, discount (10%) applies to the pre-VAT subtotal when it exceeds €100.00, VAT (21%) is applied after the discount, quantities must be positive integers, and unit prices must be non-negative. Invalid line items throw `IllegalArgumentException` collected per item.

```java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class OrderProcessor {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");

    public record LineItem(String description, BigDecimal unitPrice, int quantity) {
    }

    public record OrderResult(
            BigDecimal subtotal,
            BigDecimal discount,
            BigDecimal vat,
            BigDecimal total,
            String receipt) {
    }

    public static OrderResult processOrder(String orderId, List<LineItem> items) {
        validateItems(items);

        BigDecimal subtotal = BigDecimal.ZERO;
        StringBuilder linesText = new StringBuilder();

        for (LineItem item : items) {
            BigDecimal lineTotal = item.unitPrice()
                    .multiply(BigDecimal.valueOf(item.quantity()))
                    .setScale(2, RoundingMode.HALF_UP);
            subtotal = subtotal.add(lineTotal);

            linesText.append(String.format(Locale.UK,
                    "%-20s x%-3d %10s%n",
                    item.description(), item.quantity(), formatEur(lineTotal)));
        }

        boolean discountApplies = subtotal.compareTo(DISCOUNT_THRESHOLD) > 0;
        BigDecimal discount = discountApplies
                ? subtotal.multiply(DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal discountedSubtotal = subtotal.subtract(discount);
        BigDecimal vat = discountedSubtotal.multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = discountedSubtotal.add(vat);

        String receipt = buildReceipt(orderId, linesText.toString(), subtotal, discount, vat, total, discountApplies);

        return new OrderResult(subtotal, discount, vat, total, receipt);
    }

    private static void validateItems(List<LineItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one line item");
        }

        List<String> errors = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            LineItem item = items.get(i);
            String prefix = "Line item " + (i + 1) + ": ";

            if (item.description() == null || item.description().isBlank()) {
                errors.add(prefix + "description must not be blank");
            }
            if (item.unitPrice() == null || item.unitPrice().compareTo(BigDecimal.ZERO) < 0) {
                errors.add(prefix + "unit price must be non-negative");
            }
            if (item.quantity() <= 0) {
                errors.add(prefix + "quantity must be positive");
            }
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Invalid order line items: " + String.join("; ", errors));
        }
    }

    private static String buildReceipt(
            String orderId,
            String linesText,
            BigDecimal subtotal,
            BigDecimal discount,
            BigDecimal vat,
            BigDecimal total,
            boolean discountApplies) {

        StringBuilder sb = new StringBuilder();
        sb.append("Order: ").append(orderId).append(System.lineSeparator());
        sb.append("----------------------------------------").append(System.lineSeparator());
        sb.append(linesText);
        sb.append("----------------------------------------").append(System.lineSeparator());
        sb.append(String.format(Locale.UK, "%-25s %14s%n", "Subtotal", formatEur(subtotal)));
        if (discountApplies) {
            sb.append(String.format(Locale.UK, "%-25s %14s%n", "Discount (10%)", "-" + formatEur(discount)));
        }
        sb.append(String.format(Locale.UK, "%-25s %14s%n", "VAT (21%)", formatEur(vat)));
        sb.append(String.format(Locale.UK, "%-25s %14s%n", "Total", formatEur(total)));

        return sb.toString();
    }

    private static String formatEur(BigDecimal amount) {
        return "EUR " + amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
```

Example usage:

```java
List<OrderProcessor.LineItem> items = List.of(
        new OrderProcessor.LineItem("Lottery ticket bundle", new BigDecimal("45.00"), 3),
        new OrderProcessor.LineItem("Charity donation add-on", new BigDecimal("5.00"), 1)
);

OrderProcessor.OrderResult result = OrderProcessor.processOrder("ORD-2026-0001", items);
System.out.println(result.receipt());
```