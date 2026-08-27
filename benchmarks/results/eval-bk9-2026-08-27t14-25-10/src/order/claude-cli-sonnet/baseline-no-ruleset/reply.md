Here's a self-contained implementation using `BigDecimal` for currency precision, assuming: a line item is `(description, quantity, unitPrice)`, quantity must be positive, unit price non-negative, discount applies to the pre-VAT subtotal before VAT is calculated, and amounts are rounded to 2 decimals with `HALF_UP`.

```java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;

/**
 * Processes a customer order: validates line items, computes VAT and
 * discount, and renders a receipt.
 */
public final class OrderProcessor {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");

    public record LineItem(String description, int quantity, BigDecimal unitPrice) {
    }

    public record OrderResult(
            List<LineItem> lineItems,
            BigDecimal subtotal,
            BigDecimal discount,
            BigDecimal vat,
            BigDecimal total,
            String receipt) {
    }

    public OrderResult processOrder(List<LineItem> lineItems) {
        validateLineItems(lineItems);

        BigDecimal subtotal = lineItems.stream()
                .map(item -> item.unitPrice().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        boolean discountApplies = subtotal.compareTo(DISCOUNT_THRESHOLD) > 0;
        BigDecimal discount = discountApplies
                ? subtotal.multiply(DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal discountedSubtotal = subtotal.subtract(discount);
        BigDecimal vat = discountedSubtotal.multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = discountedSubtotal.add(vat).setScale(2, RoundingMode.HALF_UP);

        String receipt = buildReceipt(lineItems, subtotal, discount, vat, total);

        return new OrderResult(lineItems, subtotal, discount, vat, total, receipt);
    }

    private void validateLineItems(List<LineItem> lineItems) {
        if (lineItems == null || lineItems.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one line item.");
        }
        for (LineItem item : lineItems) {
            if (item.description() == null || item.description().isBlank()) {
                throw new IllegalArgumentException("Line item description must not be empty.");
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

    private String buildReceipt(
            List<LineItem> lineItems,
            BigDecimal subtotal,
            BigDecimal discount,
            BigDecimal vat,
            BigDecimal total) {
        StringBuilder sb = new StringBuilder();
        sb.append("Receipt\n");
        sb.append("-------\n");
        for (LineItem item : lineItems) {
            BigDecimal lineTotal = item.unitPrice()
                    .multiply(BigDecimal.valueOf(item.quantity()))
                    .setScale(2, RoundingMode.HALF_UP);
            sb.append(String.format(Locale.UK, "%-20s x%-3d EUR %8.2f%n",
                    item.description(), item.quantity(), lineTotal));
        }
        sb.append("-------\n");
        sb.append(String.format(Locale.UK, "Subtotal (excl. VAT): EUR %8.2f%n", subtotal));
        if (discount.compareTo(BigDecimal.ZERO) > 0) {
            sb.append(String.format(Locale.UK, "Discount (10%%):       EUR -%7.2f%n", discount));
        }
        sb.append(String.format(Locale.UK, "VAT (21%%):            EUR %8.2f%n", vat));
        sb.append(String.format(Locale.UK, "Total:                EUR %8.2f%n", total));
        return sb.toString();
    }
}
```

Notes:
- Amounts use `BigDecimal` rather than `double` to avoid floating-point rounding errors in currency math.
- Validation rejects null/blank descriptions, non-positive quantities, and negative or missing unit prices, and throws `IllegalArgumentException` with a message identifying the offending line item.
- Discount is calculated on the pre-VAT subtotal only when it strictly exceeds €100.00, then VAT is applied to the discounted amount.
- This is generic order-processing logic, not participant, prize, or draw related, so it does not trigger PLG's regulated-content or GDPR-specific handling; adapt field names and rounding rules if it feeds into a regulated workflow (for example prize payouts), and involve Finance/Compliance in that case.