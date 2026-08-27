// OrderProcessor.java
package com.plg.orders;

import java.util.List;

public final class OrderProcessor {

    private OrderProcessor() {
    }

    public static String process(List<LineItem> lineItems) {
        OrderTotals totals = OrderCalculator.calculateTotals(lineItems);
        return ReceiptFormatter.format(lineItems, totals);
    }
}
