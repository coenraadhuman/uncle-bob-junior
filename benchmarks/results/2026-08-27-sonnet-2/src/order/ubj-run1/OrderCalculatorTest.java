// OrderCalculatorTest.java
package com.example.order;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderCalculatorTest {

    @Test
    void appliesNoDiscountWhenSubtotalIsExactlyThreshold() {
        LineItem item = new LineItem("Widget", 1, new BigDecimal("100.00"));
        OrderTotals totals = OrderCalculator.calculate(List.of(item));

        assertEquals(new BigDecimal("0.00"), totals.discount());
        assertEquals(new BigDecimal("21.00"), totals.vat());
        assertEquals(new BigDecimal("121.00"), totals.total());
    }

    @Test
    void appliesDiscountWhenSubtotalExceedsThreshold() {
        LineItem item = new LineItem("Widget", 1, new BigDecimal("200.00"));
        OrderTotals totals = OrderCalculator.calculate(List.of(item));

        assertEquals(new BigDecimal("20.00"), totals.discount());
        assertEquals(new BigDecimal("37.80"), totals.vat());
        assertEquals(new BigDecimal("217.80"), totals.total());
    }

    @Test
    void sumsMultipleLineItemsBeforeApplyingDiscountAndVat() {
        List<LineItem> items = List.of(
                new LineItem("Widget", 2, new BigDecimal("30.00")),
                new LineItem("Gadget", 1, new BigDecimal("50.00")));

        OrderTotals totals = OrderCalculator.calculate(items);

        assertEquals(new BigDecimal("110.00"), totals.subtotal());
        assertEquals(new BigDecimal("11.00"), totals.discount());
        assertEquals(new BigDecimal("20.79"), totals.vat());
        assertEquals(new BigDecimal("119.79"), totals.total());
    }
}
