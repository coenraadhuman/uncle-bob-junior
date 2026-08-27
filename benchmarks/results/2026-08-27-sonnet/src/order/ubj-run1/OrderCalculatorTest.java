// OrderCalculatorTest.java
package com.plg.orders;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderCalculatorTest {

    @Test
    void appliesNoDiscountWhenSubtotalIsAtThreshold() {
        List<LineItem> items = List.of(new LineItem("Widget", new BigDecimal("50.00"), 2));

        OrderTotals totals = OrderCalculator.calculateTotals(items);

        assertEquals(new BigDecimal("100.00"), totals.subtotalBeforeDiscount());
        assertEquals(new BigDecimal("0.00"), totals.discount());
        assertEquals(new BigDecimal("21.00"), totals.vat());
        assertEquals(new BigDecimal("121.00"), totals.total());
    }

    @Test
    void appliesDiscountWhenSubtotalExceedsThreshold() {
        List<LineItem> items = List.of(new LineItem("Widget", new BigDecimal("60.00"), 2));

        OrderTotals totals = OrderCalculator.calculateTotals(items);

        assertEquals(new BigDecimal("120.00"), totals.subtotalBeforeDiscount());
        assertEquals(new BigDecimal("12.00"), totals.discount());
        assertEquals(new BigDecimal("22.68"), totals.vat());
        assertEquals(new BigDecimal("130.68"), totals.total());
    }

    @Test
    void sumsMultipleLineItems() {
        List<LineItem> items = List.of(
                new LineItem("Widget", new BigDecimal("10.00"), 3),
                new LineItem("Gadget", new BigDecimal("5.00"), 4));

        OrderTotals totals = OrderCalculator.calculateTotals(items);

        assertEquals(new BigDecimal("50.00"), totals.subtotalBeforeDiscount());
    }

    @Test
    void rejectsEmptyOrder() {
        assertThrows(IllegalArgumentException.class, () -> OrderCalculator.calculateTotals(List.of()));
    }
}
