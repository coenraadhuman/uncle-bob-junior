A few assumptions: monetary values use `BigDecimal` (currency-safe), VAT (21%) is calculated on the subtotal *after* the discount is applied, the discount threshold (€100) and rate (10%) apply strictly to the pre-VAT subtotal, and validation rejects blank descriptions, non-positive quantities, and negative unit prices.

```java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class OrderProcessor {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");

    public record LineItem(String description, int quantity, BigDecimal unitPrice) {

        public LineItem {
            if (description == null || description.isBlank()) {
                throw new IllegalArgumentException("Line item description must not be blank");
            }
            if (quantity <= 0) {
                throw new IllegalArgumentException("Line item quantity must be positive: " + quantity);
            }
            if (unitPrice == null || unitPrice.signum() < 0) {
                throw new IllegalArgumentException("Line item unit price must not be negative");
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

    public Receipt process(List<LineItem> lineItems) {
        if (lineItems == null || lineItems.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one line item");
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
        BigDecimal total = discountedSubtotal.add(vat);

        String text = buildReceiptText(lineItems, subtotal, discount, vat, total, discountApplies);

        return new Receipt(subtotal, discount, vat, total, text);
    }

    private String buildReceiptText(
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
        sb.append(String.format(Locale.UK, "Subtotal:            EUR %8.2f%n", subtotal));
        if (discountApplies) {
            sb.append(String.format(Locale.UK, "Discount (10%%):     -EUR %8.2f%n", discount));
        }
        sb.append(String.format(Locale.UK, "VAT (21%%):           EUR %8.2f%n", vat));
        sb.append(String.format(Locale.UK, "Total:               EUR %8.2f%n", total));

        return sb.toString();
    }

    public static void main(String[] args) {
        OrderProcessor processor = new OrderProcessor();
        List<LineItem> lineItems = List.of(
                new LineItem("Lottery ticket bundle", 3, new BigDecimal("25.00")),
                new LineItem("Gift wrap", 2, new BigDecimal("5.00"))
        );

        Receipt receipt = processor.process(lineItems);
        System.out.print(receipt.text());
    }
}
```