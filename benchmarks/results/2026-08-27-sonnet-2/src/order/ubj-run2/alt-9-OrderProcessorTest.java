package com.example.orders;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class OrderProcessorTest {

    private final OrderProcessor processor = new OrderProcessor();

    @Test
    void appliesNoDiscountWhenSubtotalIsAtThreshold() {
        Order order = new Order(List.of(new LineItem("Widget", new BigDecimal("100.00"), 1)));

        Receipt receipt = processor.process(order);

        assertEquals(new BigDecimal("0.00"), receipt.discount());
        assertEquals(new BigDecimal("21.00"), receipt.vat());
        assertEquals(new BigDecimal("121.00"), receipt.total());
    }

    @Test
    void appliesDiscountWhenSubtotalExceedsThreshold() {
        Order order = new Order(List.of(new LineItem("Widget", new BigDecimal("200.00"), 1)));

        Receipt receipt = processor.process(order);

        assertEquals(new BigDecimal("20.00"), receipt.discount());
        assertEquals(new BigDecimal("37.80"), receipt.vat());
        assertEquals(new BigDecimal("217.80"), receipt.total());
    }

    @Test
    void sumsMultipleLineItemsBeforeApplyingDiscountAndVat() {
        Order order = new Order(List.of(
                new LineItem("Widget", new BigDecimal("30.00"), 2),
                new LineItem("Gadget", new BigDecimal("50.00"), 1)));

        Receipt receipt = processor.process(order);

        assertEquals(new BigDecimal("110.00"), receipt.subtotal());
        assertEquals(new BigDecimal("11.00"), receipt.discount());
        assertEquals(new BigDecimal("20.79"), receipt.vat());
        assertEquals(new BigDecimal("108.79"), receipt.total());
    }
}
