// OrderProcessorTest.java
package com.example.order;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderProcessorTest {

    @Test
    void rejectsNullOrder() {
        assertThrows(EmptyOrderException.class, () -> OrderProcessor.process(null));
    }

    @Test
    void rejectsEmptyOrder() {
        assertThrows(EmptyOrderException.class, () -> OrderProcessor.process(Collections.emptyList()));
    }

    @Test
    void receiptOmitsDiscountLineWhenNoneApplies() {
        List<LineItem> items = List.of(new LineItem("Widget", 1, new BigDecimal("50.00")));
        String receipt = OrderProcessor.process(items);

        assertFalse(receipt.contains("Discount"));
        assertTrue(receipt.contains("Total"));
    }

    @Test
    void receiptIncludesDiscountLineWhenThresholdExceeded() {
        List<LineItem> items = List.of(new LineItem("Widget", 1, new BigDecimal("150.00")));
        String receipt = OrderProcessor.process(items);

        assertTrue(receipt.contains("Discount (10%)"));
    }
}
