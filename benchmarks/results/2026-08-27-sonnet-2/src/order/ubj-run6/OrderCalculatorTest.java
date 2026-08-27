package com.plg.orders;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderCalculatorTest {

    private final OrderCalculator calculator = new OrderCalculator();

    @Test
    void noDiscountWhenSubtotalAtThreshold() {
        List<LineItem> items = List.of(new LineItem("Widget", new BigDecimal("50.00"), 2));

        OrderTotals totals = calculator.calculate(items);

        assertEquals(new BigDecimal("100.00"), totals.subtotal());
        assertEquals(new BigDecimal("0.00"), totals.discount());
        assertEquals(new BigDecimal("21.00"), totals.vat());
        assertEquals(new BigDecimal("121.00"), totals.total());
    }

    @Test
    void discountAppliedWhenSubtotalExceedsThreshold() {
        List<LineItem> items = List.of(new LineItem("Widget", new BigDecimal("101.00"), 1));

        OrderTotals totals = calculator.calculate(items);

        assertEquals(new BigDecimal("101.00"), totals.subtotal());
        assertEquals(new BigDecimal("10.10"), totals.discount());
        assertEquals(new BigDecimal("19.09"), totals.vat());
        assertEquals(new BigDecimal("109.99"), totals.total());
    }

    @Test
    void sumsMultipleLineItems() {
        List<LineItem> items = List.of(
                new LineItem("Widget", new BigDecimal("10.00"), 2),
                new LineItem("Gadget", new BigDecimal("5.00"), 3));

        OrderTotals totals = calculator.calculate(items);

        assertEquals(new BigDecimal("35.00"), totals.subtotal());
    }
}
