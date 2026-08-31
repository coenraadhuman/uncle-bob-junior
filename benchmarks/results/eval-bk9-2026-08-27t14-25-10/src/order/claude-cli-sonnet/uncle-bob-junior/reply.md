Assumptions made: amounts use `BigDecimal` (money, not `double`); "exceeds 100 euros" means strictly greater than 100.00; the 10% discount applies to the pre-VAT subtotal before VAT is calculated on the discounted amount; rounding is HALF_UP to 2 decimal places; a line item is invalid if its description is blank, quantity is not positive, or unit price is negative; an order with no line items is invalid.

```java
// ---- InvalidLineItemException.java ----
public class InvalidLineItemException extends RuntimeException {
    public InvalidLineItemException(String message) {
        super(message);
    }
}

// ---- LineItem.java ----
import java.math.BigDecimal;

public record LineItem(String description, BigDecimal unitPrice, int quantity) {

    public LineItem {
        requireNonBlankDescription(description);
        requireNonNegativePrice(unitPrice);
        requirePositiveQuantity(quantity);
    }

    private static void requireNonBlankDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new InvalidLineItemException("Line item description must not be blank");
        }
    }

    private static void requireNonNegativePrice(BigDecimal unitPrice) {
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidLineItemException("Line item unit price must not be negative");
        }
    }

    private static void requirePositiveQuantity(int quantity) {
        if (quantity <= 0) {
            throw new InvalidLineItemException("Line item quantity must be positive");
        }
    }

    public BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}

// ---- OrderTotals.java ----
import java.math.BigDecimal;

public record OrderTotals(BigDecimal subtotal, BigDecimal discount, BigDecimal vat, BigDecimal total) {
}

// ---- OrderCalculator.java ----
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public final class OrderCalculator {

    static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final int MONEY_SCALE = 2;

    private OrderCalculator() {
    }

    public static OrderTotals computeTotals(List<LineItem> lineItems) {
        requireNonEmpty(lineItems);
        BigDecimal subtotal = sumLineTotals(lineItems);
        BigDecimal discount = calculateDiscount(subtotal);
        BigDecimal discountedSubtotal = subtotal.subtract(discount);
        BigDecimal vat = round(discountedSubtotal.multiply(VAT_RATE));
        BigDecimal total = discountedSubtotal.add(vat);
        return new OrderTotals(round(subtotal), discount, vat, round(total));
    }

    private static void requireNonEmpty(List<LineItem> lineItems) {
        if (lineItems == null || lineItems.isEmpty()) {
            throw new InvalidLineItemException("Order must contain at least one line item");
        }
    }

    private static BigDecimal sumLineTotals(List<LineItem> lineItems) {
        return lineItems.stream()
                .map(LineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal calculateDiscount(BigDecimal subtotal) {
        boolean qualifiesForDiscount = subtotal.compareTo(DISCOUNT_THRESHOLD) > 0;
        return qualifiesForDiscount ? round(subtotal.multiply(DISCOUNT_RATE)) : BigDecimal.ZERO.setScale(MONEY_SCALE);
    }

    private static BigDecimal round(BigDecimal value) {
        return value.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}

// ---- ReceiptFormatter.java ----
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

public final class ReceiptFormatter {

    private ReceiptFormatter() {
    }

    public static String format(List<LineItem> lineItems, OrderTotals totals) {
        StringBuilder receipt = new StringBuilder();
        appendHeader(receipt);
        appendLineItems(receipt, lineItems);
        appendTotals(receipt, totals);
        return receipt.toString();
    }

    private static void appendHeader(StringBuilder receipt) {
        receipt.append("Receipt\n").append("-------\n");
    }

    private static void appendLineItems(StringBuilder receipt, List<LineItem> lineItems) {
        for (LineItem item : lineItems) {
            receipt.append(formatLineItem(item)).append('\n');
        }
    }

    private static String formatLineItem(LineItem item) {
        return String.format(Locale.UK, "%dx %-20s EUR %8.2f",
                item.quantity(), item.description(), item.lineTotal());
    }

    private static void appendTotals(StringBuilder receipt, OrderTotals totals) {
        receipt.append("-------\n");
        receipt.append(formatMoneyLine("Subtotal", totals.subtotal()));
        if (totals.discount().compareTo(BigDecimal.ZERO) > 0) {
            receipt.append(formatMoneyLine("Discount (10%)", totals.discount().negate()));
        }
        receipt.append(formatMoneyLine("VAT (21%)", totals.vat()));
        receipt.append(formatMoneyLine("Total", totals.total()));
    }

    private static String formatMoneyLine(String label, BigDecimal amount) {
        return String.format(Locale.UK, "%-20s EUR %8.2f%n", label, amount);
    }
}

// ---- OrderProcessor.java ----
import java.util.List;

public final class OrderProcessor {

    private OrderProcessor() {
    }

    public static String process(List<LineItem> lineItems) {
        OrderTotals totals = OrderCalculator.computeTotals(lineItems);
        return ReceiptFormatter.format(lineItems, totals);
    }
}
```

```java
// ---- OrderProcessorTest.java (JUnit 5) ----
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderProcessorTest {

    @Test
    void blankDescriptionIsRejected() {
        assertThrows(InvalidLineItemException.class,
                () -> new LineItem(" ", new BigDecimal("10.00"), 1));
    }

    @Test
    void zeroQuantityIsRejected() {
        assertThrows(InvalidLineItemException.class,
                () -> new LineItem("Ticket", new BigDecimal("10.00"), 0));
    }

    @Test
    void negativeUnitPriceIsRejected() {
        assertThrows(InvalidLineItemException.class,
                () -> new LineItem("Ticket", new BigDecimal("-1.00"), 1));
    }

    @Test
    void emptyOrderIsRejected() {
        assertThrows(InvalidLineItemException.class,
                () -> OrderCalculator.computeTotals(List.of()));
    }

    @Test
    void subtotalBelowThresholdHasNoDiscount() {
        List<LineItem> items = List.of(new LineItem("Ticket", new BigDecimal("20.00"), 2));

        OrderTotals totals = OrderCalculator.computeTotals(items);

        assertEquals(new BigDecimal("40.00"), totals.subtotal());
        assertEquals(new BigDecimal("0.00"), totals.discount());
        assertEquals(new BigDecimal("8.40"), totals.vat());
        assertEquals(new BigDecimal("48.40"), totals.total());
    }

    @Test
    void subtotalAtThresholdHasNoDiscount() {
        List<LineItem> items = List.of(new LineItem("Ticket", new BigDecimal("100.00"), 1));

        OrderTotals totals = OrderCalculator.computeTotals(items);

        assertEquals(new BigDecimal("0.00"), totals.discount());
        assertEquals(new BigDecimal("121.00"), totals.total());
    }

    @Test
    void subtotalAboveThresholdGetsDiscount() {
        List<LineItem> items = List.of(new LineItem("Ticket", new BigDecimal("150.00"), 1));

        OrderTotals totals = OrderCalculator.computeTotals(items);

        assertEquals(new BigDecimal("15.00"), totals.discount());
        assertEquals(new BigDecimal("28.35"), totals.vat());
        assertEquals(new BigDecimal("163.35"), totals.total());
    }

    @Test
    void receiptContainsExpectedTotalLine() {
        List<LineItem> items = List.of(new LineItem("Ticket", new BigDecimal("150.00"), 1));

        String receipt = OrderProcessor.process(items);

        assertTrue(receipt.contains("Total"));
        assertTrue(receipt.contains("163.35"));
    }
}
```

`→ cleaned: validation lives in `LineItem` itself so an invalid order can't be constructed, totals math is isolated in `OrderCalculator`, formatting in `ReceiptFormatter` — safe to change because each piece has one reason to change and is tested independently of the others.`