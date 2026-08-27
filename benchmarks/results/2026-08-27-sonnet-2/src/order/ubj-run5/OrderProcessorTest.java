// OrderProcessorTest.java
package com.plg.order;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderProcessorTest {

    private final OrderProcessor processor = new OrderProcessor();

    @Test
    void rejectsEmptyOrder() {
        assertThrows(IllegalArgumentException.class, () -> processor.process(List.of()));
    }

    @Test
    void appliesNoDiscountWhenSubtotalIsAtThreshold() {
        OrderLineItem item = new OrderLineItem("Widget", new BigDecimal("100.00"), 1);

        Receipt receipt = processor.process(List.of(item));

        assertEquals(new BigDecimal("0.00"), receipt.discount());
        assertEquals(new BigDecimal("21.00"), receipt.vat());
        assertEquals(new BigDecimal("121.00"), receipt.total());
    }

    @Test
    void appliesDiscountWhenSubtotalExceedsThreshold() {
        OrderLineItem item = new OrderLineItem("Widget", new BigDecimal("150.00"), 1);

        Receipt receipt = processor.process(List.of(item));

        assertEquals(new BigDecimal("150.00"), receipt.subtotal());
        assertEquals(new BigDecimal("15.00"), receipt.discount());
        assertEquals(new BigDecimal("28.35"), receipt.vat());
        assertEquals(new BigDecimal("163.35"), receipt.total());
    }

    @Test
    void sumsMultipleLineItemsBeforeApplyingDiscountAndVat() {
        List<OrderLineItem> items = List.of(
                new OrderLineItem("Widget", new BigDecimal("40.00"), 2),
                new OrderLineItem("Gadget", new BigDecimal("25.00"), 1));

        Receipt receipt = processor.process(items);

        assertEquals(new BigDecimal("105.00"), receipt.subtotal());
        assertEquals(new BigDecimal("10.50"), receipt.discount());
    }
}
