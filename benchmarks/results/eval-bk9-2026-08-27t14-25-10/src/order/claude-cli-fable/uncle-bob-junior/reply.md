Assumptions: amounts are euros as `BigDecimal` (HALF_UP rounding to cents), the 10% discount applies when the pre-VAT subtotal is strictly over EUR 100.00, VAT is then charged on the discounted amount, and an order must have at least one valid line item.

```java
import java.math.BigDecimal;

/**
 * One order line. Validated at construction, so a LineItem that exists is valid.
 * unitPrice is in euros, non-negative; quantity is at least 1.
 */
public record LineItem(String description, BigDecimal unitPrice, int quantity) {

    public LineItem {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Line item needs a description");
        }
        if (unitPrice == null || unitPrice.signum() < 0) {
            throw new IllegalArgumentException("Unit price must be zero or positive");
        }
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }
    }

    public BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
```

```java
import java.math.BigDecimal;

/** Immutable breakdown of an order's money amounts, all rounded to cents. */
public record OrderTotals(BigDecimal subtotal, BigDecimal discount, BigDecimal vat, BigDecimal total) {
}
```

```java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/** Pure calculation of order totals: no I/O, no mutation. */
public final class OrderCalculator {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final int CENT_SCALE = 2;

    public OrderTotals totalsFor(List<LineItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("An order needs at least one line item");
        }
        BigDecimal subtotal = roundToCents(subtotalOf(items));
        BigDecimal discount = discountOn(subtotal);
        // ubj: business rule per spec, VAT is charged on the discounted amount
        BigDecimal taxable = subtotal.subtract(discount);
        BigDecimal vat = roundToCents(taxable.multiply(VAT_RATE));
        return new OrderTotals(subtotal, discount, vat, taxable.add(vat));
    }

    private BigDecimal subtotalOf(List<LineItem> items) {
        return items.stream()
                .map(LineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal discountOn(BigDecimal subtotal) {
        if (subtotal.compareTo(DISCOUNT_THRESHOLD) <= 0) {
            return BigDecimal.ZERO.setScale(CENT_SCALE, RoundingMode.HALF_UP);
        }
        return roundToCents(subtotal.multiply(DISCOUNT_RATE));
    }

    private static BigDecimal roundToCents(BigDecimal amount) {
        return amount.setScale(CENT_SCALE, RoundingMode.HALF_UP);
    }
}
```

```java
import java.util.List;
import java.util.Locale;

/** Turns validated line items and computed totals into a human-readable receipt. */
public final class ReceiptFormatter {

    private static final String CURRENCY = "EUR";

    public String format(List<LineItem> items, OrderTotals totals) {
        StringBuilder receipt = new StringBuilder("RECEIPT\n");
        items.forEach(item -> receipt.append(lineFor(item)));
        receipt.append(row("Subtotal:", totals.subtotal()));
        if (totals.discount().signum() > 0) {
            receipt.append(row("Discount (10%):", totals.discount().negate()));
        }
        receipt.append(row("VAT (21%):", totals.vat()));
        receipt.append(row("Total:", totals.total()));
        return receipt.toString();
    }

    private String lineFor(LineItem item) {
        return String.format(Locale.ROOT, "%s x %d @ %s %s = %s %s%n",
                item.description(), item.quantity(),
                CURRENCY, item.unitPrice(),
                CURRENCY, item.lineTotal());
    }

    private String row(String label, Object amount) {
        return String.format(Locale.ROOT, "%-16s %s %s%n", label, CURRENCY, amount);
    }
}
```

```java
import java.util.List;

/** Single entry point: validated items in, receipt string out. */
public final class OrderProcessor {

    private final OrderCalculator calculator = new OrderCalculator();
    private final ReceiptFormatter formatter = new ReceiptFormatter();

    public String process(List<LineItem> items) {
        OrderTotals totals = calculator.totalsFor(items);
        return formatter.format(items, totals);
    }
}
```

```java
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderProcessorTest {

    private final OrderCalculator calculator = new OrderCalculator();
    private final OrderProcessor processor = new OrderProcessor();

    private static LineItem item(String price, int quantity) {
        return new LineItem("Widget", new BigDecimal(price), quantity);
    }

    @Test
    void computesVatWithoutDiscountBelowThreshold() {
        OrderTotals totals = calculator.totalsFor(List.of(item("10.00", 3)));
        assertEquals(new BigDecimal("30.00"), totals.subtotal());
        assertEquals(new BigDecimal("0.00"), totals.discount());
        assertEquals(new BigDecimal("6.30"), totals.vat());
        assertEquals(new BigDecimal("36.30"), totals.total());
    }

    @Test
    void appliesDiscountWhenSubtotalExceedsThreshold() {
        OrderTotals totals = calculator.totalsFor(List.of(item("60.00", 2)));
        assertEquals(new BigDecimal("120.00"), totals.subtotal());
        assertEquals(new BigDecimal("12.00"), totals.discount());
        assertEquals(new BigDecimal("22.68"), totals.vat());
        assertEquals(new BigDecimal("130.68"), totals.total());
    }

    @Test
    void noDiscountAtExactlyOneHundredEuros() {
        OrderTotals totals = calculator.totalsFor(List.of(item("100.00", 1)));
        assertEquals(new BigDecimal("0.00"), totals.discount());
        assertEquals(new BigDecimal("121.00"), totals.total());
    }

    @Test
    void rejectsEmptyOrNullOrder() {
        assertThrows(IllegalArgumentException.class, () -> calculator.totalsFor(List.of()));
        assertThrows(IllegalArgumentException.class, () -> calculator.totalsFor(null));
    }

    @Test
    void rejectsInvalidLineItems() {
        assertThrows(IllegalArgumentException.class, () -> new LineItem(" ", new BigDecimal("1.00"), 1));
        assertThrows(IllegalArgumentException.class, () -> new LineItem("Widget", new BigDecimal("-1.00"), 1));
        assertThrows(IllegalArgumentException.class, () -> new LineItem("Widget", null, 1));
        assertThrows(IllegalArgumentException.class, () -> new LineItem("Widget", new BigDecimal("1.00"), 0));
    }

    @Test
    void receiptListsItemsAndAllTotalRows() {
        String receipt = processor.process(List.of(item("60.00", 2)));
        assertTrue(receipt.startsWith("RECEIPT"));
        assertTrue(receipt.contains("Widget x 2 @ EUR 60.00 = EUR 120.00"));
        assertTrue(receipt.contains("Discount (10%):"));
        assertTrue(receipt.contains("VAT (21%):"));
        assertTrue(receipt.contains("Total:           EUR 130.68"));
    }

    @Test
    void receiptOmitsDiscountRowWhenNoDiscountApplies() {
        String receipt = processor.process(List.of(item("10.00", 1)));
        assertFalse(receipt.contains("Discount"));
        assertTrue(receipt.contains("Total:           EUR 12.10"));
    }
}
```

Cleaned: validation lives once in `LineItem`'s constructor, money maths is isolated in a pure `OrderCalculator` with named rate and threshold constants, and formatting is a separate concern. Safe to change because the calculator is side-effect free and every rule (VAT, discount boundary, invalid input, receipt shape) is pinned by a test.