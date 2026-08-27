package com.plg.orders;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderProcessorTest {

    private final OrderProcessor processor = new OrderProcessor();

    @Test
    void producesReceiptForValidOrder() {
        List<LineItem> items = List.of(new LineItem("Widget", new BigDecimal("60.00"), 2));

        OrderResult<String> result = processor.process(items);

        OrderResult.Success<String> success = assertInstanceOf(OrderResult.Success.class, result);
        assertTrue(success.value().contains("Discount (10%): -EUR 12.00"));
        assertTrue(success.value().contains("Total: EUR 130.68"));
    }

    @Test
    void failsWithErrorsForInvalidOrder() {
        OrderResult<String> result = processor.process(List.of());

        OrderResult.Failure<String> failure = assertInstanceOf(OrderResult.Failure.class, result);
        assertTrue(failure.errors().contains("Order must contain at least one line item"));
    }
}
