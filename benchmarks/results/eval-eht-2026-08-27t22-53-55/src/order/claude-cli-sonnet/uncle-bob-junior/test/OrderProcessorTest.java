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
