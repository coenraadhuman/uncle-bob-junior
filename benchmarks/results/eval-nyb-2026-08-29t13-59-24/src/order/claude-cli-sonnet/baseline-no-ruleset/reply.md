Assumptions: amounts use `BigDecimal` (EUR, 2 decimal places, HALF_UP rounding); a line item is invalid if description is blank, quantity ≤ 0, or unit price is negative; the 10% discount applies to the pre-VAT subtotal when that subtotal exceeds €100, and VAT is calculated on the discounted amount.

```java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

public final class OrderProcessor {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");

    public record LineItem(String description, int quantity, BigDecimal unitPrice) {

        public BigDecimal lineTotal() {
            return unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    public record OrderResult(
            BigDecimal subtotal,
            BigDecimal discount,
            BigDecimal totalExVat,
            BigDecimal vat,
            BigDecimal totalIncVat,
            String receipt
    ) {}

    public static void validateLineItems(List<LineItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one line item");
        }
        for (LineItem item : items) {
            if (item.description() == null || item.description().isBlank()) {
                throw new IllegalArgumentException("Line item description must not be blank");
            }
            if (item.quantity() <= 0) {
                throw new IllegalArgumentException(
                        "Line item '%s' must have a positive quantity".formatted(item.description()));
            }
            if (item.unitPrice() == null || item.unitPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException(
                        "Line item '%s' must have a non-negative unit price".formatted(item.description()));
            }
        }
    }

    public static OrderResult processOrder(String orderId, List<LineItem> items) {
        Objects.requireNonNull(orderId, "orderId must not be null");
        validateLineItems(items);

        BigDecimal subtotal = items.stream()
                .map(LineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        boolean discountApplies = subtotal.compareTo(DISCOUNT_THRESHOLD) > 0;
        BigDecimal discount = discountApplies
                ? subtotal.multiply(DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal totalExVat = subtotal.subtract(discount).setScale(2, RoundingMode.HALF_UP);
        BigDecimal vat = totalExVat.multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalIncVat = totalExVat.add(vat).setScale(2, RoundingMode.HALF_UP);

        String receipt = buildReceipt(orderId, items, subtotal, discount, totalExVat, vat, totalIncVat);

        return new OrderResult(subtotal, discount, totalExVat, vat, totalIncVat, receipt);
    }

    private static String buildReceipt(
            String orderId,
            List<LineItem> items,
            BigDecimal subtotal,
            BigDecimal discount,
            BigDecimal totalExVat,
            BigDecimal vat,
            BigDecimal totalIncVat
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append("Order: ").append(orderId).append('\n');
        sb.append("-".repeat(40)).append('\n');

        for (LineItem item : items) {
            sb.append("%-20s x%-4d %8s%n".formatted(
                    item.description(),
                    item.quantity(),
                    formatEuro(item.lineTotal())));
        }

        sb.append("-".repeat(40)).append('\n');
        sb.append("Subtotal (ex. VAT):   %8s%n".formatted(formatEuro(subtotal)));
        if (discount.compareTo(BigDecimal.ZERO) > 0) {
            sb.append("Discount (10%%):       %8s%n".formatted(formatEuro(discount.negate())));
        }
        sb.append("Total ex. VAT:        %8s%n".formatted(formatEuro(totalExVat)));
        sb.append("VAT (21%%):            %8s%n".formatted(formatEuro(vat)));
        sb.append("Total inc. VAT:       %8s%n".formatted(formatEuro(totalIncVat)));

        return sb.toString();
    }

    private static String formatEuro(BigDecimal amount) {
        return "EUR " + amount.setScale(2, RoundingMode.HALF_UP);
    }

    public static void main(String[] args) {
        List<LineItem> items = List.of(
                new LineItem("Lottery ticket bundle", 3, new BigDecimal("25.00")),
                new LineItem("Charity donation add-on", 1, new BigDecimal("30.00"))
        );

        OrderResult result = processOrder("ORD-2026-0001", items);
        System.out.println(result.receipt());
    }
}
```

Note: this is a plain calculation utility with no persistence or external calls; if it will feed customer-facing invoices or financial reporting, have Finance/Compliance confirm the VAT and discount rules before use in production.