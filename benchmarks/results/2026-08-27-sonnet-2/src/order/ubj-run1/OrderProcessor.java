// OrderProcessor.java
package com.example.order;

import java.util.List;

public final class OrderProcessor {

    private OrderProcessor() {
    }

    public static String process(List<LineItem> lineItems) {
        if (lineItems == null || lineItems.isEmpty()) {
            throw new EmptyOrderException("An order must contain at least one line item");
        }
        OrderTotals totals = OrderCalculator.calculate(lineItems);
        return ReceiptFormatter.format(lineItems, totals);
    }
}
