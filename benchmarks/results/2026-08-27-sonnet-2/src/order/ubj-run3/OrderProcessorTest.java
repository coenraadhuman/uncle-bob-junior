package com.example.orders;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderProcessorTest {

    private final OrderProcessor processor = new OrderProcessor();

    @Test
    void noDiscountWhenSubtotalAtThreshold() {
        List<LineItem> items = List.of(new LineItem("Widget", new BigDecimal("50.00"), 2));

        OrderTotals totals = processor.calculateTotals(items);

        assertEquals(new BigDecimal("100.00"), totals.subtotal());
        assertEquals(new BigDecimal("0.00"), totals.discount());
        assertEquals(new BigDecimal("21.00"), totals.vat());
        assertEquals(new BigDecimal("121.00"), totals.total());
    }

    @Test
    void discountAndVatAppliedWhenSubtotalExceedsThreshold() {
        List<LineItem> items = List.of(new LineItem("Widget", new BigDecimal("60.00"), 2));

        OrderTotals totals = processor.calculateTotals(items);

        assertEquals(new BigDecimal("120.00"), totals.subtotal());
        assertEquals(new BigDecimal("12.00"), totals.discount());
        assertEquals(new BigDecimal("22.68"), totals.vat());
        assertEquals(new BigDecimal("130.68"), totals.total());
    }

    @Test
    void belowThresholdHasNoDiscount() {
        List<LineItem> items = List.of(new LineItem("Widget", new BigDecimal("50.00"), 1));

        OrderTotals totals = processor.calculateTotals(items);

        assertEquals(new BigDecimal("0.00"), totals.discount());
        assertEquals(new BigDecimal("60.50"), totals.total());
    }

    @Test
    void rejectsEmptyOrder() {
        assertThrows(InvalidLineItemException.class, () -> processor.calculateTotals(List.of()));
    }

    @Test
    void rejectsNullOrder() {
        assertThrows(InvalidLineItemException.class, () -> processor.calculateTotals(null));
    }
}
