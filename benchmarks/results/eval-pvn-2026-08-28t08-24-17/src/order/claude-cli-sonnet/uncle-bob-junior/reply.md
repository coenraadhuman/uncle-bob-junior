Assumptions: EUR currency, VAT fixed at 21%, discount applies only when pre-VAT subtotal is strictly greater than €100.00, one `Order` = a non-empty list of line items, and money uses `BigDecimal` with HALF_UP rounding to 2 decimals. Test data below is synthetic.

```java
// LineItem.java
import java.math.BigDecimal;
import java.util.Objects;

public record LineItem(String description, BigDecimal unitPrice, int quantity) {

    public LineItem {
        Objects.requireNonNull(description, "description must not be null");
        Objects.requireNonNull(unitPrice, "unitPrice must not be null");
        if (description.isBlank()) {
            throw new IllegalArgumentException("description must not be blank");
        }
        if (unitPrice.signum() < 0) {
            throw new IllegalArgumentException("unitPrice must not be negative: " + unitPrice);
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive: " + quantity);
        }
    }

    public BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
```

```java
// OrderSummary.java
import java.math.BigDecimal;

public record OrderSummary(BigDecimal subtotal, BigDecimal discount, BigDecimal vatAmount, BigDecimal total) {
}
```

```java
// OrderCalculator.java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

public final class OrderCalculator {

    static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final int MONEY_SCALE = 2;

    public OrderSummary summarize(List<LineItem> lineItems) {
        validateOrder(lineItems);
        BigDecimal subtotal = computeSubtotal(lineItems);
        BigDecimal discount = computeDiscount(subtotal);
        BigDecimal netAfterDiscount = round(subtotal.subtract(discount));
        BigDecimal vatAmount = computeVat(netAfterDiscount);
        BigDecimal total = round(netAfterDiscount.add(vatAmount));
        return new OrderSummary(round(subtotal), discount, vatAmount, total);
    }

    private void validateOrder(List<LineItem> lineItems) {
        Objects.requireNonNull(lineItems, "lineItems must not be null");
        if (lineItems.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one line item");
        }
    }

    private BigDecimal computeSubtotal(List<LineItem> lineItems) {
        return lineItems.stream()
            .map(LineItem::lineTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal computeDiscount(BigDecimal subtotal) {
        if (subtotal.compareTo(DISCOUNT_THRESHOLD) <= 0) {
            return round(BigDecimal.ZERO);
        }
        return round(subtotal.multiply(DISCOUNT_RATE));
    }

    private BigDecimal computeVat(BigDecimal netAmount) {
        return round(netAmount.multiply(VAT_RATE));
    }

    private BigDecimal round(BigDecimal amount) {
        return amount.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
```

```java
// ReceiptFormatter.java
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

public final class ReceiptFormatter {

    private static final String SEPARATOR = "--------------";
    private static final String LINE_BREAK = System.lineSeparator();

    public String format(List<LineItem> lineItems, OrderSummary summary) {
        StringBuilder receipt = new StringBuilder();
        appendHeader(receipt);
        appendLineItems(receipt, lineItems);
        appendSummary(receipt, summary);
        return receipt.toString();
    }

    private void appendHeader(StringBuilder receipt) {
        receipt.append("Order Receipt").append(LINE_BREAK);
        receipt.append(SEPARATOR).append(LINE_BREAK);
    }

    private void appendLineItems(StringBuilder receipt, List<LineItem> lineItems) {
        for (LineItem item : lineItems) {
            receipt.append(formatLineItem(item)).append(LINE_BREAK);
        }
        receipt.append(SEPARATOR).append(LINE_BREAK);
    }

    private String formatLineItem(LineItem item) {
        return "%s x%d @ %s = %s".formatted(
            item.description(), item.quantity(), formatMoney(item.unitPrice()), formatMoney(item.lineTotal()));
    }

    private void appendSummary(StringBuilder receipt, OrderSummary summary) {
        String vatLabel = "VAT (%s%%): ".formatted(
            OrderCalculator.VAT_RATE.multiply(BigDecimal.valueOf(100)).stripTrailingZeros().toPlainString());
        receipt.append("Subtotal: ").append(formatMoney(summary.subtotal())).append(LINE_BREAK);
        receipt.append("Discount: -").append(formatMoney(summary.discount())).append(LINE_BREAK);
        receipt.append(vatLabel).append(formatMoney(summary.vatAmount())).append(LINE_BREAK);
        receipt.append("Total: ").append(formatMoney(summary.total())).append(LINE_BREAK);
    }

    private String formatMoney(BigDecimal amount) {
        return String.format(Locale.ROOT, "\u20AC%,.2f", amount);
    }
}
```

```java
// LineItemTest.java
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LineItemTest {

    @Test
    void rejectsBlankDescription() {
        assertThrows(IllegalArgumentException.class,
            () -> new LineItem("  ", new BigDecimal("10.00"), 1));
    }

    @Test
    void rejectsNegativePrice() {
        assertThrows(IllegalArgumentException.class,
            () -> new LineItem("participant_001", new BigDecimal("-1.00"), 1));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void rejectsNonPositiveQuantity(int quantity) {
        assertThrows(IllegalArgumentException.class,
            () -> new LineItem("participant_001", new BigDecimal("10.00"), quantity));
    }

    @Test
    void lineTotalMultipliesPriceByQuantity() {
        LineItem item = new LineItem("participant_001", new BigDecimal("10.00"), 3);

        assertEquals(new BigDecimal("30.00"), item.lineTotal());
    }
}
```

```java
// OrderCalculatorTest.java
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderCalculatorTest {

    private final OrderCalculator calculator = new OrderCalculator();

    @Test
    void throwsOnEmptyOrder() {
        assertThrows(IllegalArgumentException.class, () -> calculator.summarize(List.of()));
    }

    @Test
    void throwsOnNullOrder() {
        assertThrows(NullPointerException.class, () -> calculator.summarize(null));
    }

    @Test
    void noDiscountWhenSubtotalDoesNotExceedThreshold() {
        LineItem item = new LineItem("participant_001", new BigDecimal("50.00"), 2);

        OrderSummary summary = calculator.summarize(List.of(item));

        assertEquals(new BigDecimal("100.00"), summary.subtotal());
        assertEquals(new BigDecimal("0.00"), summary.discount());
        assertEquals(new BigDecimal("21.00"), summary.vatAmount());
        assertEquals(new BigDecimal("121.00"), summary.total());
    }

    @Test
    void appliesDiscountWhenSubtotalExceedsThreshold() {
        LineItem item = new LineItem("participant_001", new BigDecimal("60.00"), 2);

        OrderSummary summary = calculator.summarize(List.of(item));

        assertEquals(new BigDecimal("120.00"), summary.subtotal());
        assertEquals(new BigDecimal("12.00"), summary.discount());
        assertEquals(new BigDecimal("22.68"), summary.vatAmount());
        assertEquals(new BigDecimal("130.68"), summary.total());
    }
}
```

```java
// ReceiptFormatterTest.java
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ReceiptFormatterTest {

    private final OrderCalculator calculator = new OrderCalculator();
    private final ReceiptFormatter formatter = new ReceiptFormatter();

    @Test
    void formatIncludesLineItemAndAllSummaryFigures() {
        LineItem item = new LineItem("participant_001", new BigDecimal("60.00"), 2);
        OrderSummary summary = calculator.summarize(List.of(item));

        String receipt = formatter.format(List.of(item), summary);

        assertTrue(receipt.contains("participant_001 x2"));
        assertTrue(receipt.contains("Subtotal: \u20AC120.00"));
        assertTrue(receipt.contains("Discount: -\u20AC12.00"));
        assertTrue(receipt.contains("VAT (21%): \u20AC22.68"));
        assertTrue(receipt.contains("Total: \u20AC130.68"));
    }
}
```

`LineItem` makes invalid orders unrepresentable at construction, `OrderCalculator` isolates the money math as pure functions, `ReceiptFormatter` isolates presentation → cleaned: separated concerns with named VAT/discount constants, safe to change because each rate or format tweak touches exactly one class and its own tests.