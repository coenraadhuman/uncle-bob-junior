Assumptions: EUR amounts as `BigDecimal` (never `double` for money); VAT is applied *after* the discount on the discounted subtotal; the 10% discount applies only when the pre-VAT subtotal is **strictly greater than** €100.00; each line item needs a non-blank description, unit price > 0, and quantity > 0; an order needs at least one line item; validation failures are reported all at once via a custom exception rather than failing on the first bad item.

```java
// RawLineItem.java
import java.math.BigDecimal;

/** Unvalidated input for a single order line, as supplied by the caller. */
public record RawLineItem(String description, BigDecimal unitPrice, int quantity) {
}
```

```java
// LineItem.java
import java.math.BigDecimal;

/**
 * A line item whose invariants (non-blank description, positive price and
 * quantity) have been checked. Only {@link LineItemValidator} constructs these.
 */
public record LineItem(String description, BigDecimal unitPrice, int quantity, BigDecimal lineTotal) {
}
```

```java
// OrderTotals.java
import java.math.BigDecimal;

/** Result of totalling an order: subtotal, discount, VAT and the final total. */
public record OrderTotals(BigDecimal subtotal, BigDecimal discountAmount, BigDecimal vatAmount, BigDecimal total) {
}
```

```java
// OrderValidationException.java
import java.util.List;

public final class OrderValidationException extends RuntimeException {

    private final List<String> errors;

    public OrderValidationException(List<String> errors) {
        super(String.join("; ", errors));
        this.errors = List.copyOf(errors);
    }

    public List<String> errors() {
        return errors;
    }
}
```

```java
// MoneyFormat.java
import java.math.RoundingMode;

/** Shared rounding rules for money amounts, kept in one place. */
final class MoneyFormat {

    static final int SCALE = 2;
    static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    private MoneyFormat() {
    }
}
```

```java
// LineItemValidator.java
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public final class LineItemValidator {

    public List<LineItem> validate(List<RawLineItem> rawItems) {
        if (rawItems.isEmpty()) {
            throw new OrderValidationException(List.of("Order must contain at least one line item"));
        }
        List<String> errors = new ArrayList<>();
        List<LineItem> validated = new ArrayList<>();
        for (int index = 0; index < rawItems.size(); index++) {
            validateItem(rawItems.get(index), index, errors, validated);
        }
        if (!errors.isEmpty()) {
            throw new OrderValidationException(errors);
        }
        return List.copyOf(validated);
    }

    private void validateItem(RawLineItem raw, int index, List<String> errors, List<LineItem> validated) {
        List<String> itemErrors = describeErrors(raw, index);
        if (!itemErrors.isEmpty()) {
            errors.addAll(itemErrors);
            return;
        }
        validated.add(toLineItem(raw));
    }

    private List<String> describeErrors(RawLineItem raw, int index) {
        List<String> errors = new ArrayList<>();
        if (raw.description() == null || raw.description().isBlank()) {
            errors.add("Line item " + (index + 1) + ": description must not be blank");
        }
        if (raw.unitPrice() == null || raw.unitPrice().signum() <= 0) {
            errors.add("Line item " + (index + 1) + ": unit price must be positive");
        }
        if (raw.quantity() <= 0) {
            errors.add("Line item " + (index + 1) + ": quantity must be positive");
        }
        return errors;
    }

    private LineItem toLineItem(RawLineItem raw) {
        BigDecimal lineTotal = raw.unitPrice()
                .multiply(BigDecimal.valueOf(raw.quantity()))
                .setScale(MoneyFormat.SCALE, MoneyFormat.ROUNDING);
        return new LineItem(raw.description(), raw.unitPrice(), raw.quantity(), lineTotal);
    }
}
```

```java
// OrderCalculator.java
import java.math.BigDecimal;
import java.util.List;

public final class OrderCalculator {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");

    public OrderTotals calculate(List<LineItem> items) {
        BigDecimal subtotal = sumLineTotals(items);
        BigDecimal discountAmount = discountFor(subtotal);
        BigDecimal discountedSubtotal = subtotal.subtract(discountAmount);
        BigDecimal vatAmount = vatFor(discountedSubtotal);
        BigDecimal total = discountedSubtotal.add(vatAmount);
        return new OrderTotals(subtotal, discountAmount, vatAmount, total);
    }

    private BigDecimal sumLineTotals(List<LineItem> items) {
        return items.stream()
                .map(LineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(MoneyFormat.SCALE, MoneyFormat.ROUNDING);
    }

    private BigDecimal discountFor(BigDecimal subtotal) {
        if (subtotal.compareTo(DISCOUNT_THRESHOLD) <= 0) {
            return BigDecimal.ZERO.setScale(MoneyFormat.SCALE, MoneyFormat.ROUNDING);
        }
        return subtotal.multiply(DISCOUNT_RATE).setScale(MoneyFormat.SCALE, MoneyFormat.ROUNDING);
    }

    private BigDecimal vatFor(BigDecimal discountedSubtotal) {
        return discountedSubtotal.multiply(VAT_RATE).setScale(MoneyFormat.SCALE, MoneyFormat.ROUNDING);
    }
}
```

```java
// ReceiptFormatter.java
import java.util.List;

public final class ReceiptFormatter {

    public String format(List<LineItem> items, OrderTotals totals) {
        StringBuilder receipt = new StringBuilder();
        appendHeader(receipt);
        appendLineItems(receipt, items);
        appendTotals(receipt, totals);
        return receipt.toString();
    }

    private void appendHeader(StringBuilder receipt) {
        receipt.append("Receipt").append(System.lineSeparator());
        receipt.append("-------").append(System.lineSeparator());
    }

    private void appendLineItems(StringBuilder receipt, List<LineItem> items) {
        for (LineItem item : items) {
            receipt.append(formatLine(item)).append(System.lineSeparator());
        }
    }

    private String formatLine(LineItem item) {
        return "%s x%d @ %s = %s".formatted(item.description(), item.quantity(), item.unitPrice(), item.lineTotal());
    }

    private void appendTotals(StringBuilder receipt, OrderTotals totals) {
        receipt.append("-------").append(System.lineSeparator());
        receipt.append("Subtotal: ").append(totals.subtotal()).append(System.lineSeparator());
        receipt.append("Discount: -").append(totals.discountAmount()).append(System.lineSeparator());
        receipt.append("VAT (21%): ").append(totals.vatAmount()).append(System.lineSeparator());
        receipt.append("Total: ").append(totals.total());
    }
}
```

```java
// OrderProcessor.java
import java.util.List;

public final class OrderProcessor {

    private final LineItemValidator validator = new LineItemValidator();
    private final OrderCalculator calculator = new OrderCalculator();
    private final ReceiptFormatter formatter = new ReceiptFormatter();

    public String process(List<RawLineItem> rawItems) {
        List<LineItem> items = validator.validate(rawItems);
        OrderTotals totals = calculator.calculate(items);
        return formatter.format(items, totals);
    }
}
```

```java
// LineItemValidatorTest.java
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LineItemValidatorTest {

    private final LineItemValidator validator = new LineItemValidator();

    @Test
    void acceptsWellFormedLineItems() {
        List<RawLineItem> raw = List.of(new RawLineItem("Ticket", new BigDecimal("10.00"), 2));

        List<LineItem> result = validator.validate(raw);

        assertEquals(new BigDecimal("20.00"), result.get(0).lineTotal());
    }

    @Test
    void rejectsEmptyOrder() {
        OrderValidationException exception =
                assertThrows(OrderValidationException.class, () -> validator.validate(List.of()));

        assertTrue(exception.errors().get(0).contains("at least one line item"));
    }

    @Test
    void rejectsBlankDescription() {
        List<RawLineItem> raw = List.of(new RawLineItem(" ", new BigDecimal("10.00"), 1));

        OrderValidationException exception =
                assertThrows(OrderValidationException.class, () -> validator.validate(raw));

        assertTrue(exception.errors().get(0).contains("description"));
    }

    @Test
    void rejectsNonPositiveUnitPrice() {
        List<RawLineItem> raw = List.of(new RawLineItem("Ticket", BigDecimal.ZERO, 1));

        OrderValidationException exception =
                assertThrows(OrderValidationException.class, () -> validator.validate(raw));

        assertTrue(exception.errors().get(0).contains("unit price"));
    }

    @Test
    void rejectsNonPositiveQuantity() {
        List<RawLineItem> raw = List.of(new RawLineItem("Ticket", new BigDecimal("10.00"), 0));

        OrderValidationException exception =
                assertThrows(OrderValidationException.class, () -> validator.validate(raw));

        assertTrue(exception.errors().get(0).contains("quantity"));
    }

    @Test
    void aggregatesErrorsAcrossMultipleLineItems() {
        List<RawLineItem> raw = List.of(
                new RawLineItem("", new BigDecimal("10.00"), 1),
                new RawLineItem("Ticket", new BigDecimal("-5.00"), 1));

        OrderValidationException exception =
                assertThrows(OrderValidationException.class, () -> validator.validate(raw));

        assertEquals(2, exception.errors().size());
    }
}
```

```java
// OrderCalculatorTest.java
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderCalculatorTest {

    private final OrderCalculator calculator = new OrderCalculator();

    @Test
    void appliesNoDiscountAtOrBelowThreshold() {
        LineItem item = new LineItem("Ticket", new BigDecimal("100.00"), 1, new BigDecimal("100.00"));

        OrderTotals totals = calculator.calculate(List.of(item));

        assertEquals(new BigDecimal("0.00"), totals.discountAmount());
        assertEquals(new BigDecimal("121.00"), totals.total());
    }

    @Test
    void appliesDiscountWhenSubtotalExceedsThreshold() {
        LineItem item = new LineItem("Ticket", new BigDecimal("200.00"), 1, new BigDecimal("200.00"));

        OrderTotals totals = calculator.calculate(List.of(item));

        assertEquals(new BigDecimal("20.00"), totals.discountAmount());
    }

    @Test
    void computesVatOnDiscountedSubtotal() {
        LineItem item = new LineItem("Ticket", new BigDecimal("200.00"), 1, new BigDecimal("200.00"));

        OrderTotals totals = calculator.calculate(List.of(item));

        assertEquals(new BigDecimal("37.80"), totals.vatAmount());
        assertEquals(new BigDecimal("217.80"), totals.total());
    }
}
```

```java
// OrderProcessorTest.java
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderProcessorTest {

    private final OrderProcessor processor = new OrderProcessor();

    @Test
    void producesReceiptForValidOrder() {
        List<RawLineItem> raw = List.of(new RawLineItem("Ticket", new BigDecimal("50.00"), 3));

        String receipt = processor.process(raw);

        assertTrue(receipt.contains("Subtotal: 150.00"));
        assertTrue(receipt.contains("Discount: -15.00"));
        assertTrue(receipt.contains("Total: 141.75"));
    }

    @Test
    void propagatesValidationFailure() {
        assertThrows(OrderValidationException.class, () -> processor.process(List.of()));
    }
}
```

**→ cleaned:** validation, totalling, and formatting split into single-responsibility classes with a `RawLineItem` → `LineItem` boundary parse and named rate/scale constants. **Safe to change because** each piece is independently tested (validator, calculator, formatter, orchestrator), so a rounding or wording tweak in one won't silently break the others.