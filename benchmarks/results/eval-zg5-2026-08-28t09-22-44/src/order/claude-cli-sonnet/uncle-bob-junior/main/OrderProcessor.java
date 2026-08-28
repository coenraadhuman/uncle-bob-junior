// File: OrderProcessor.java
package com.postcodeloterij.orders;

import java.util.List;

public final class OrderProcessor {

    private final LineItemValidator validator;
    private final OrderPricingPolicy pricingPolicy;
    private final ReceiptFormatter receiptFormatter;

    public OrderProcessor(LineItemValidator validator, OrderPricingPolicy pricingPolicy, ReceiptFormatter receiptFormatter) {
        this.validator = validator;
        this.pricingPolicy = pricingPolicy;
        this.receiptFormatter = receiptFormatter;
    }

    public OrderProcessor() {
        this(new LineItemValidator(), new OrderPricingPolicy(), new ReceiptFormatter());
    }

    /**
     * Validates the line items, computes VAT and any discount, and returns the receipt text.
     * @throws OrderValidationException if any line item is invalid or the order is empty
     */
    public String process(List<LineItem> lineItems) {
        validator.validateAll(lineItems);
        OrderTotals totals = pricingPolicy.calculate(lineItems);
        return receiptFormatter.format(lineItems, totals);
    }
}
