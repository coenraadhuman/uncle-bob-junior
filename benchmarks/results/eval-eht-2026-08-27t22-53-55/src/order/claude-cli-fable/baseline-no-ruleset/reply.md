Here's a complete, self-contained solution. Assumptions: monetary values use `BigDecimal` (EUR, rounded half-up to 2 decimal places), the 10% discount applies when the pre-VAT subtotal is strictly greater than €100.00, the discount is applied before VAT is calculated, and validation rejects null/blank names, non-positive quantities, and negative unit prices.

```java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

/**
 * Processes an order: validates line items, applies a 10% discount when the
 * pre-VAT subtotal exceeds EUR 100.00, adds 21% VAT, and renders a receipt.
 */
public final class OrderProcessor {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    /** A single order line: item name, quantity, and unit price excluding VAT. */
    public record LineItem(String name, int quantity, BigDecimal unitPrice) {

        public LineItem {
            Objects.requireNonNull(unitPrice, "unitPrice must not be null");
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Line item name must not be blank");
            }
            if (quantity <= 0) {
                throw new IllegalArgumentException(
                        "Quantity must be positive for item '" + name + "', got: " + quantity);
            }
            if (unitPrice.signum() < 0) {
                throw new IllegalArgumentException(
                        "Unit price must not be negative for item '" + name + "', got: " + unitPrice);
            }
        }

        public BigDecimal lineTotal() {
            return unitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(SCALE, ROUNDING);
        }
    }

    /** Computed totals for an order. All amounts in EUR, rounded to 2 decimal places. */
    public record OrderTotals(
            BigDecimal subtotal,
            BigDecimal discount,
            BigDecimal netAfterDiscount,
            BigDecimal vat,
            BigDecimal total) {
    }

    private OrderProcessor() {
    }

    /** Validates the order and computes all totals. */
    public static OrderTotals computeTotals(List<LineItem> items) {
        validate(items);

        BigDecimal subtotal = items.stream()
                .map(LineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(SCALE, ROUNDING);

        BigDecimal discount = subtotal.compareTo(DISCOUNT_THRESHOLD) > 0
                ? subtotal.multiply(DISCOUNT_RATE).setScale(SCALE, ROUNDING)
                : BigDecimal.ZERO.setScale(SCALE, ROUNDING);

        BigDecimal net = subtotal.subtract(discount);
        BigDecimal vat = net.multiply(VAT_RATE).setScale(SCALE, ROUNDING);
        BigDecimal total = net.add(vat);

        return new OrderTotals(subtotal, discount, net, vat, total);
    }

    private static void validate(List<LineItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one line item");
        }
        // Record constructor enforces per-item rules; reject null entries here.
        if (items.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Order must not contain null line items");
        }
    }

    /** Produces a plain-text receipt for the given order. */
    public static String buildReceipt(String orderId, List<LineItem> items) {
        OrderTotals totals = computeTotals(items);
        StringBuilder sb = new StringBuilder();

        sb.append("RECEIPT").append(System.lineSeparator());
        sb.append("Order: ").append(orderId).append(System.lineSeparator());
        sb.append("----------------------------------------").append(System.lineSeparator());

        for (LineItem item : items) {
            sb.append(String.format("%-20s %3d x %8s = %9s%n",
                    truncate(item.name(), 20),
                    item.quantity(),
                    "EUR " + item.unitPrice().setScale(SCALE, ROUNDING),
                    "EUR " + item.lineTotal()));
        }

        sb.append("----------------------------------------").append(System.lineSeparator());
        appendRow(sb, "Subtotal (excl. VAT)", totals.subtotal());
        if (totals.discount().signum() > 0) {
            appendRow(sb, "Discount (10%)", totals.discount().negate());
            appendRow(sb, "Net after discount", totals.netAfterDiscount());
        }
        appendRow(sb, "VAT (21%)", totals.vat());
        sb.append("----------------------------------------").append(System.lineSeparator());
        appendRow(sb, "TOTAL", totals.total());

        return sb.toString();
    }

    private static void appendRow(StringBuilder sb, String label, BigDecimal amount) {
        sb.append(String.format("%-26s EUR %9s%n", label, amount.toPlainString()));
    }

    private static String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + ".";
    }

    public static void main(String[] args) {
        List<LineItem> order = List.of(
                new LineItem("Notebook", 3, new BigDecimal("12.50")),
                new LineItem("Desk lamp", 1, new BigDecimal("45.00")),
                new LineItem("USB-C cable", 2, new BigDecimal("14.75"))
        );

        System.out.println(buildReceipt("ORD-2026-0001", order));
    }
}
```

Running `main` produces a receipt like this:

```
RECEIPT
Order: ORD-2026-0001
----------------------------------------
Notebook               3 x EUR 12.50 = EUR 37.50
Desk lamp              1 x EUR 45.00 = EUR 45.00
USB-C cable            2 x EUR 14.75 = EUR 29.50
----------------------------------------
Subtotal (excl. VAT)       EUR    112.00
Discount (10%)             EUR    -11.20
Net after discount         EUR    100.80
VAT (21%)                  EUR     21.17
----------------------------------------
TOTAL                      EUR    121.97
```

A few notes on the design: `BigDecimal` with `HALF_UP` rounding avoids the floating-point errors you'd get with `double` for money. Validation lives in the `LineItem` record constructor so invalid items can never exist, with an order-level check for null or empty lists. If your business rules differ (for example, discount applied after VAT, or a threshold of "at least" rather than "more than" €100), those are one-line changes in `computeTotals`. For anything customer-facing at PLG, the receipt wording and VAT treatment should be confirmed with Finance/Tax before use; this code is a starting point, not tax advice.