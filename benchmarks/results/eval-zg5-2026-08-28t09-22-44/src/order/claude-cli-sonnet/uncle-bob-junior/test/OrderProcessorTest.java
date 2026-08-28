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
