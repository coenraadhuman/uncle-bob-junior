Assumptions: amounts use `BigDecimal` with HALF_UP rounding to 2 decimals; "exceeds 100" means strictly greater than 100.00; the 10% discount applies to the pre-VAT subtotal before VAT is calculated; an order must contain at least one line item. Classes below would live in separate files in a real project (`LineItem.java`, `OrderTotals.java`, `OrderCalculator.java`, `ReceiptFormatter.java`, `OrderProcessor.java`), package `com.plg.orders`.

```java
package com.plg.orders;

import java.math.BigDecimal;
import java.util.Objects;

public record LineItem(String description, BigDecimal unitPrice, int quantity) {

    public LineItem {
        Objects.requireNonNull(unitPrice, "unitPrice must not be null");
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("description must not be blank");
        }
        if (unitPrice.signum() <= 0) {
            throw new IllegalArgumentException("unitPrice must be positive");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
    }

    public BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
```

```java
package com.plg.orders;

import java.math.BigDecimal;

public record OrderTotals(
        BigDecimal subtotalBeforeDiscount,
        BigDecimal discountAmount,
        BigDecimal subtotalAfterDiscount,
        BigDecimal vatAmount,
        BigDecimal total) {
}
```

```java
package com.plg.orders;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public final class OrderCalculator {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final int MONEY_SCALE = 2;

    public OrderTotals calculate(List<LineItem> lineItems) {
        if (lineItems == null || lineItems.isEmpty()) {
            throw new IllegalArgumentException("an order must contain at least one line item");
        }

        BigDecimal subtotal = sumLineTotals(lineItems);
        BigDecimal discount = calculateDiscount(subtotal);
        BigDecimal subtotalAfterDiscount = round(subtotal.subtract(discount));
        BigDecimal vat = round(subtotalAfterDiscount.multiply(VAT_RATE));
        BigDecimal total = round(subtotalAfterDiscount.add(vat));

        return new OrderTotals(round(subtotal), discount, subtotalAfterDiscount, vat, total);
    }

    private BigDecimal sumLineTotals(List<LineItem> lineItems) {
        return lineItems.stream()
                .map(LineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateDiscount(BigDecimal subtotal) {
        boolean qualifiesForDiscount = subtotal.compareTo(DISCOUNT_THRESHOLD) > 0;
        if (!qualifiesForDiscount) {
            return BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        }
        return round(subtotal.multiply(DISCOUNT_RATE));
    }

    private BigDecimal round(BigDecimal amount) {
        return amount.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
```

```java
package com.plg.orders;

import java.math.BigDecimal;
import java.util.List;

public final class ReceiptFormatter {

    private static final String CURRENCY_SUFFIX = " EUR";

    public String format(List<LineItem> lineItems, OrderTotals totals) {
        StringBuilder receipt = new StringBuilder();
        appendLineItems(receipt, lineItems);
        appendTotals(receipt, totals);
        return receipt.toString();
    }

    private void appendLineItems(StringBuilder receipt, List<LineItem> lineItems) {
        for (LineItem item : lineItems) {
            receipt.append(formatLine(item)).append(System.lineSeparator());
        }
    }

    private String formatLine(LineItem item) {
        return "%dx %s @ %s%s = %s%s".formatted(
                item.quantity(), item.description(),
                money(item.unitPrice()), CURRENCY_SUFFIX,
                money(item.lineTotal()), CURRENCY_SUFFIX);
    }

    private void appendTotals(StringBuilder receipt, OrderTotals totals) {
        receipt.append("Subtotal: ").append(money(totals.subtotalBeforeDiscount()))
                .append(CURRENCY_SUFFIX).append(System.lineSeparator());
        if (totals.discountAmount().signum() > 0) {
            receipt.append("Discount: -").append(money(totals.discountAmount()))
                    .append(CURRENCY_SUFFIX).append(System.lineSeparator());
        }
        receipt.append("VAT (21%): ").append(money(totals.vatAmount()))
                .append(CURRENCY_SUFFIX).append(System.lineSeparator());
        receipt.append("Total: ").append(money(totals.total())).append(CURRENCY_SUFFIX);
    }

    private String money(BigDecimal amount) {
        return amount.toPlainString();
    }
}
```

```java
package com.plg.orders;

import java.util.List;

public final class OrderProcessor {

    private final OrderCalculator calculator = new OrderCalculator();
    private final ReceiptFormatter formatter = new ReceiptFormatter();

    public String process(List<LineItem> lineItems) {
        OrderTotals totals = calculator.calculate(lineItems);
        return formatter.format(lineItems, totals);
    }
}
```

```java
package com.plg.orders;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class LineItemTest {

    @Test
    void createsValidLineItem() {
        LineItem item = new LineItem("Widget", new BigDecimal("10.00"), 3);
        assertEquals(new BigDecimal("30.00"), item.lineTotal());
    }

    @Test
    void rejectsBlankDescription() {
        assertThrows(IllegalArgumentException.class,
                () -> new LineItem(" ", new BigDecimal("10.00"), 1));
    }

    @Test
    void rejectsNonPositiveUnitPrice() {
        assertThrows(IllegalArgumentException.class,
                () -> new LineItem("Widget", BigDecimal.ZERO, 1));
    }

    @Test
    void rejectsNonPositiveQuantity() {
        assertThrows(IllegalArgumentException.class,
                () -> new LineItem("Widget", new BigDecimal("10.00"), 0));
    }
}
```

```java
package com.plg.orders;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class OrderCalculatorTest {

    private final OrderCalculator calculator = new OrderCalculator();

    @Test
    void appliesNoDiscountWhenSubtotalAtThreshold() {
        LineItem item = new LineItem("Widget", new BigDecimal("100.00"), 1);
        OrderTotals totals = calculator.calculate(List.of(item));

        assertEquals(new BigDecimal("0.00"), totals.discountAmount());
        assertEquals(new BigDecimal("121.00"), totals.total());
    }

    @Test
    void appliesDiscountWhenSubtotalExceedsThreshold() {
        LineItem item = new LineItem("Widget", new BigDecimal("50.00"), 3);
        OrderTotals totals = calculator.calculate(List.of(item));

        assertEquals(new BigDecimal("15.00"), totals.discountAmount());
        assertEquals(new BigDecimal("163.35"), totals.total());
    }

    @Test
    void rejectsEmptyOrder() {
        assertThrows(IllegalArgumentException.class, () -> calculator.calculate(List.of()));
    }

    @Test
    void rejectsNullOrder() {
        assertThrows(IllegalArgumentException.class, () -> calculator.calculate(null));
    }
}
```

```java
package com.plg.orders;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ReceiptFormatterTest {

    private final ReceiptFormatter formatter = new ReceiptFormatter();

    @Test
    void includesDiscountLineWhenDiscountApplied() {
        LineItem item = new LineItem("Widget", new BigDecimal("50.00"), 3);
        OrderTotals totals = new OrderCalculator().calculate(List.of(item));

        String receipt = formatter.format(List.of(item), totals);

        assertTrue(receipt.contains("Discount: -15.00 EUR"));
        assertTrue(receipt.contains("Total: 163.35 EUR"));
    }

    @Test
    void omitsDiscountLineWhenNoDiscountApplied() {
        LineItem item = new LineItem("Widget", new BigDecimal("10.00"), 1);
        OrderTotals totals = new OrderCalculator().calculate(List.of(item));

        String receipt = formatter.format(List.of(item), totals);

        assertFalse(receipt.contains("Discount"));
    }
}
```

```java
package com.plg.orders;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class OrderProcessorTest {

    @Test
    void producesReceiptForValidOrder() {
        OrderProcessor processor = new OrderProcessor();
        LineItem item = new LineItem("Widget", new BigDecimal("10.00"), 1);

        String receipt = processor.process(List.of(item));

        assertTrue(receipt.contains("Total: 12.10 EUR"));
    }
}
```

Cleaned: split into `LineItem` (validated value object), `OrderCalculator` (pure money math), `ReceiptFormatter` (presentation), `OrderProcessor` (orchestration). Safe to change because validation, calculation, and formatting each live in one place and are covered by tests for the discount boundary, VAT math, and invalid input.