package com.plg.orders;

import java.util.List;

public final class OrderProcessor {

    private final OrderCalculator calculator = new OrderCalculator();
    private final ReceiptFormatter formatter = new ReceiptFormatter();

    public String process(List<LineItem> lineItems) {
        OrderTotals totals = calculator.calculate(lineItems);
        return formatter.format(lineItems, totals);
    }
}
