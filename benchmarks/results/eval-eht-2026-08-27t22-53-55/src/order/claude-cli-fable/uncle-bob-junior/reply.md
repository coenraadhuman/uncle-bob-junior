Assumptions: discount applies when the pre-VAT subtotal strictly exceeds €100.00 and is taken before VAT, VAT is charged on the discounted subtotal, amounts round half-up to cents, and monetary values use `BigDecimal` (never `double`).

```java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/** One order line. Quantity must be positive, unit price non-negative, name non-blank. */
public record LineItem(String name, int quantity, BigDecimal unitPrice) {

    public LineItem {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Line item name must not be blank");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive, was " + quantity);
        }
        if (unitPrice == null || unitPrice.signum() < 0) {
            throw new IllegalArgumentException("Unit price must be non-negative");
        }
    }

    public BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
```

```java
import java.util.List;

/** A validated, immutable order: at least one line item. */
public record Order(List<LineItem> items) {

    public Order {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one line item");
        }
        items = List.copyOf(items);
    }
}
```

```java
import java.math.BigDecimal;
import java.util.List;

/**
 * Priced order result. All amounts are in EUR, rounded to cents.
 * Invariant: total = subtotal - discount + vat.
 */
public record Receipt(List<LineItem> items,
                      BigDecimal subtotal,
                      BigDecimal discount,
                      BigDecimal vat,
                      BigDecimal total) {

    public Receipt {
        items = List.copyOf(items);
    }

    public String format() {
        StringBuilder receipt = new StringBuilder("RECEIPT\n");
        items.forEach(item -> receipt.append(formatLine(item)));
        appendAmount(receipt, "Subtotal", subtotal);
        if (discount.signum() > 0) {
            appendAmount(receipt, "Discount", discount.negate());
        }
        appendAmount(receipt, "VAT (21%)", vat);
        appendAmount(receipt, "Total", total);
        return receipt.toString();
    }

    private static String formatLine(LineItem item) {
        return String.format("%s x%d @ EUR %s = EUR %s%n",
                item.name(), item.quantity(),
                item.unitPrice().toPlainString(),
                item.lineTotal().toPlainString());
    }

    private static void appendAmount(StringBuilder receipt, String label, BigDecimal amount) {
        receipt.append(String.format("%s: EUR %s%n", label, amount.toPlainString()));
    }
}
```

```java
import java.math.BigDecimal;
import java.math.RoundingMode;

/** Prices an order: subtotal, threshold discount, VAT, and grand total. */
public final class OrderProcessor {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final int CENTS = 2;

    public Receipt process(Order order) {
        BigDecimal subtotal = roundToCents(subtotalOf(order));
        BigDecimal discount = discountFor(subtotal);
        BigDecimal discountedSubtotal = subtotal.subtract(discount);
        BigDecimal vat = roundToCents(discountedSubtotal.multiply(VAT_RATE));
        BigDecimal total = discountedSubtotal.add(vat);
        return new Receipt(order.items(), subtotal, discount, vat, total);
    }

    private static BigDecimal subtotalOf(Order order) {
        return order.items().stream()
                .map(LineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal discountFor(BigDecimal subtotal) {
        if (subtotal.compareTo(DISCOUNT_THRESHOLD) <= 0) {
            return BigDecimal.ZERO.setScale(CENTS);
        }
        return roundToCents(subtotal.multiply(DISCOUNT_RATE));
    }

    private static BigDecimal roundToCents(BigDecimal amount) {
        return amount.setScale(CENTS, RoundingMode.HALF_UP);
    }
}
```

```java
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderProcessorTest {

    private final OrderProcessor processor = new OrderProcessor();

    private static LineItem item(String name, int quantity, String unitPrice) {
        return new LineItem(name, quantity, new BigDecimal(unitPrice));
    }

    @Test
    void pricesOrderBelowThresholdWithoutDiscount() {
        Receipt receipt = processor.process(new Order(List.of(item("Ticket", 2, "25.00"))));

        assertEquals(new BigDecimal("50.00"), receipt.subtotal());
        assertEquals(new BigDecimal("0.00"), receipt.discount());
        assertEquals(new BigDecimal("10.50"), receipt.vat());
        assertEquals(new BigDecimal("60.50"), receipt.total());
    }

    @Test
    void appliesTenPercentDiscountAboveHundredEuros() {
        Receipt receipt = processor.process(new Order(List.of(item("Bundle", 1, "200.00"))));

        assertEquals(new BigDecimal("200.00"), receipt.subtotal());
        assertEquals(new BigDecimal("20.00"), receipt.discount());
        assertEquals(new BigDecimal("37.80"), receipt.vat());
        assertEquals(new BigDecimal("217.80"), receipt.total());
    }

    @Test
    void exactlyHundredEurosGetsNoDiscount() {
        Receipt receipt = processor.process(new Order(List.of(item("Bundle", 4, "25.00"))));

        assertEquals(new BigDecimal("0.00"), receipt.discount());
        assertEquals(new BigDecimal("121.00"), receipt.total());
    }

    @Test
    void roundsVatHalfUpToCents() {
        // 33.33 * 0.21 = 6.9993, rounds to 7.00
        Receipt receipt = processor.process(new Order(List.of(item("Ticket", 1, "33.33"))));

        assertEquals(new BigDecimal("7.00"), receipt.vat());
        assertEquals(new BigDecimal("40.33"), receipt.total());
    }

    @Test
    void formatsReceiptWithAllAmounts() {
        Receipt receipt = processor.process(new Order(List.of(item("Bundle", 1, "200.00"))));

        String text = receipt.format();
        assertTrue(text.contains("Bundle x1 @ EUR 200.00 = EUR 200.00"));
        assertTrue(text.contains("Subtotal: EUR 200.00"));
        assertTrue(text.contains("Discount: EUR -20.00"));
        assertTrue(text.contains("VAT (21%): EUR 37.80"));
        assertTrue(text.contains("Total: EUR 217.80"));
    }

    @Test
    void receiptOmitsDiscountLineWhenNoDiscountApplies() {
        Receipt receipt = processor.process(new Order(List.of(item("Ticket", 1, "10.00"))));

        assertFalse(receipt.format().contains("Discount"));
    }

    @Test
    void rejectsInvalidLineItems() {
        assertThrows(IllegalArgumentException.class, () -> item(" ", 1, "10.00"));
        assertThrows(IllegalArgumentException.class, () -> item("Ticket", 0, "10.00"));
        assertThrows(IllegalArgumentException.class, () -> item("Ticket", -1, "10.00"));
        assertThrows(IllegalArgumentException.class, () -> item("Ticket", 1, "-0.01"));
    }

    @Test
    void rejectsEmptyOrder() {
        assertThrows(IllegalArgumentException.class, () -> new Order(List.of()));
        assertThrows(IllegalArgumentException.class, () -> new Order(null));
    }
}
```

Cleaned: validation lives once in the record constructors, so invalid orders are unrepresentable past the boundary; rates and the threshold are named constants; pricing is a pure function of an immutable `Order`. Safe to change because the VAT rate, discount rate, and threshold each exist in exactly one place, and the tests pin the boundary at €100.00 and the rounding behaviour.