Assumptions: amounts are `BigDecimal` in EUR, rounded half-up to 2 decimals; "exceeds €100" means strictly greater than 100.00; a line item needs a non-blank description, quantity ≥ 1, and a non-negative unit price; each class below goes in its own file under `com.postcodeloterij.orders`.

```java
// File: LineItem.java
package com.postcodeloterij.orders;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * One item on an order. Amounts are in EUR.
 */
public record LineItem(String description, int quantity, BigDecimal unitPrice) {

    public LineItem {
        Objects.requireNonNull(description, "description must not be null");
        Objects.requireNonNull(unitPrice, "unitPrice must not be null");
    }

    public BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
```

```java
// File: OrderValidationException.java
package com.postcodeloterij.orders;

public final class OrderValidationException extends RuntimeException {

    public OrderValidationException(String message) {
        super(message);
    }
}
```

```java
// File: LineItemValidator.java
package com.postcodeloterij.orders;

import java.math.BigDecimal;
import java.util.List;

public final class LineItemValidator {

    private static final int MINIMUM_QUANTITY = 1;
    private static final BigDecimal MINIMUM_UNIT_PRICE = BigDecimal.ZERO;

    public void validateAll(List<LineItem> lineItems) {
        if (lineItems.isEmpty()) {
            throw new OrderValidationException("Order must contain at least one line item");
        }
        lineItems.forEach(this::validate);
    }

    public void validate(LineItem item) {
        if (item.description().isBlank()) {
            throw new OrderValidationException("Line item description must not be blank");
        }
        if (item.quantity() < MINIMUM_QUANTITY) {
            throw new OrderValidationException("Line item quantity must be at least " + MINIMUM_QUANTITY);
        }
        if (item.unitPrice().compareTo(MINIMUM_UNIT_PRICE) < 0) {
            throw new OrderValidationException("Line item unit price must not be negative");
        }
    }
}
```

```java
// File: OrderTotals.java
package com.postcodeloterij.orders;

import java.math.BigDecimal;

/**
 * Pricing breakdown for an order, all fields in EUR rounded to 2 decimals.
 * discountAmount is always non-negative; it is the amount subtracted, not a negative delta.
 */
public record OrderTotals(BigDecimal preDiscountSubtotal,
                           BigDecimal discountAmount,
                           BigDecimal vatAmount,
                           BigDecimal grandTotal) {
}
```

```java
// File: OrderPricingPolicy.java
package com.postcodeloterij.orders;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public final class OrderPricingPolicy {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final int MONEY_SCALE = 2;

    public OrderTotals calculate(List<LineItem> lineItems) {
        BigDecimal subtotal = sumLineTotals(lineItems);
        BigDecimal discount = calculateDiscount(subtotal);
        BigDecimal discountedSubtotal = subtotal.subtract(discount);
        BigDecimal vatAmount = round(discountedSubtotal.multiply(VAT_RATE));
        BigDecimal grandTotal = round(discountedSubtotal.add(vatAmount));
        return new OrderTotals(round(subtotal), discount, vatAmount, grandTotal);
    }

    private BigDecimal sumLineTotals(List<LineItem> lineItems) {
        return lineItems.stream()
                .map(LineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateDiscount(BigDecimal subtotal) {
        boolean qualifiesForDiscount = subtotal.compareTo(DISCOUNT_THRESHOLD) > 0;
        if (!qualifiesForDiscount) {
            return round(BigDecimal.ZERO);
        }
        return round(subtotal.multiply(DISCOUNT_RATE));
    }

    private BigDecimal round(BigDecimal amount) {
        return amount.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
```

```java
// File: ReceiptFormatter.java
package com.postcodeloterij.orders;

import java.math.BigDecimal;

public final class ReceiptFormatter {

    private static final String CURRENCY = "EUR";
    private static final String NEWLINE = System.lineSeparator();
    private static final String RECEIPT_TITLE = "Receipt";

    public String format(Iterable<LineItem> lineItems, OrderTotals totals) {
        StringBuilder receipt = new StringBuilder();
        receipt.append(RECEIPT_TITLE).append(NEWLINE);
        lineItems.forEach(item -> appendLineItem(receipt, item));
        appendTotalsSection(receipt, totals);
        return receipt.toString();
    }

    private void appendLineItem(StringBuilder receipt, LineItem item) {
        receipt.append(item.description())
                .append(" x").append(item.quantity())
                .append(": ").append(formatAmount(item.lineTotal())).append(" ").append(CURRENCY)
                .append(NEWLINE);
    }

    private void appendTotalsSection(StringBuilder receipt, OrderTotals totals) {
        receipt.append("Subtotal: ").append(formatAmount(totals.preDiscountSubtotal())).append(" ").append(CURRENCY).append(NEWLINE);
        receipt.append("Discount: -").append(formatAmount(totals.discountAmount())).append(" ").append(CURRENCY).append(NEWLINE);
        receipt.append("VAT (21%): ").append(formatAmount(totals.vatAmount())).append(" ").append(CURRENCY).append(NEWLINE);
        receipt.append("Total: ").append(formatAmount(totals.grandTotal())).append(" ").append(CURRENCY).append(NEWLINE);
    }

    private String formatAmount(BigDecimal amount) {
        return amount.toPlainString();
    }
}
```

```java
// File: OrderProcessor.java
package com.postcodeloterij.orders;

import java.util.List;

public final class OrderProcessor {

    private final LineItemValidator validator;
    private final OrderPricingPolicy pricingPolicy;
    private final ReceiptFormatter receiptFormatter;

    public OrderProcessor(LineItemValidator validator, OrderPricingPolicy pricingPolicy, ReceiptFormatter receiptFormatter) {
        this.validator = validator;
        this.pricingPolicy = pricingPolicy;
        this.receiptFormatter = receiptFormatter;
    }

    public OrderProcessor() {
        this(new LineItemValidator(), new OrderPricingPolicy(), new ReceiptFormatter());
    }

    /**
     * Validates the line items, computes VAT and any discount, and returns the receipt text.
     * @throws OrderValidationException if any line item is invalid or the order is empty
     */
    public String process(List<LineItem> lineItems) {
        validator.validateAll(lineItems);
        OrderTotals totals = pricingPolicy.calculate(lineItems);
        return receiptFormatter.format(lineItems, totals);
    }
}
```

```java
// File: LineItemValidatorTest.java
package com.postcodeloterij.orders;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LineItemValidatorTest {

    private final LineItemValidator validator = new LineItemValidator();

    @Test
    void acceptsAValidLineItem() {
        LineItem item = new LineItem("Widget", 2, new BigDecimal("9.99"));
        assertDoesNotThrow(() -> validator.validate(item));
    }

    @Test
    void rejectsBlankDescription() {
        LineItem item = new LineItem("   ", 1, new BigDecimal("9.99"));
        assertThrows(OrderValidationException.class, () -> validator.validate(item));
    }

    @Test
    void rejectsZeroQuantity() {
        LineItem item = new LineItem("Widget", 0, new BigDecimal("9.99"));
        assertThrows(OrderValidationException.class, () -> validator.validate(item));
    }

    @Test
    void rejectsNegativeQuantity() {
        LineItem item = new LineItem("Widget", -1, new BigDecimal("9.99"));
        assertThrows(OrderValidationException.class, () -> validator.validate(item));
    }

    @Test
    void rejectsNegativeUnitPrice() {
        LineItem item = new LineItem("Widget", 1, new BigDecimal("-0.01"));
        assertThrows(OrderValidationException.class, () -> validator.validate(item));
    }

    @Test
    void rejectsEmptyOrder() {
        assertThrows(OrderValidationException.class, () -> validator.validateAll(List.of()));
    }
}
```

```java
// File: OrderPricingPolicyTest.java
package com.postcodeloterij.orders;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderPricingPolicyTest {

    private final OrderPricingPolicy pricingPolicy = new OrderPricingPolicy();

    @Test
    void appliesNoDiscountBelowThreshold() {
        List<LineItem> items = List.of(new LineItem("Widget", 2, new BigDecimal("30.00")));

        OrderTotals totals = pricingPolicy.calculate(items);

        assertEquals(new BigDecimal("60.00"), totals.preDiscountSubtotal());
        assertEquals(new BigDecimal("0.00"), totals.discountAmount());
        assertEquals(new BigDecimal("12.60"), totals.vatAmount());
        assertEquals(new BigDecimal("72.60"), totals.grandTotal());
    }

    @Test
    void appliesNoDiscountAtExactThreshold() {
        List<LineItem> items = List.of(new LineItem("Widget", 2, new BigDecimal("50.00")));

        OrderTotals totals = pricingPolicy.calculate(items);

        assertEquals(new BigDecimal("0.00"), totals.discountAmount());
        assertEquals(new BigDecimal("121.00"), totals.grandTotal());
    }

    @Test
    void appliesDiscountAboveThreshold() {
        List<LineItem> items = List.of(new LineItem("Widget", 3, new BigDecimal("50.00")));

        OrderTotals totals = pricingPolicy.calculate(items);

        assertEquals(new BigDecimal("150.00"), totals.preDiscountSubtotal());
        assertEquals(new BigDecimal("15.00"), totals.discountAmount());
        assertEquals(new BigDecimal("28.35"), totals.vatAmount());
        assertEquals(new BigDecimal("163.35"), totals.grandTotal());
    }
}
```

```java
// File: OrderProcessorTest.java
package com.postcodeloterij.orders;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderProcessorTest {

    private final OrderProcessor processor = new OrderProcessor();

    @Test
    void producesReceiptWithDiscountApplied() {
        List<LineItem> items = List.of(new LineItem("Widget", 3, new BigDecimal("50.00")));

        String receipt = processor.process(items);

        assertTrue(receipt.contains("Discount: -15.00 EUR"));
        assertTrue(receipt.contains("Total: 163.35 EUR"));
    }

    @Test
    void producesReceiptWithoutDiscount() {
        List<LineItem> items = List.of(new LineItem("Widget", 2, new BigDecimal("30.00")));

        String receipt = processor.process(items);

        assertTrue(receipt.contains("Discount: -0.00 EUR"));
        assertTrue(receipt.contains("Total: 72.60 EUR"));
    }

    @Test
    void rejectsAnInvalidOrder() {
        List<LineItem> items = List.of(new LineItem("", 1, new BigDecimal("10.00")));

        assertThrows(OrderValidationException.class, () -> processor.process(items));
    }
}
```

→ cleaned: validation, pricing, and formatting are three separate classes wired by `OrderProcessor`, each testable on its own; safe to change because a discount-rule or VAT-rate change touches only `OrderPricingPolicy` without risking the validation or formatting logic.