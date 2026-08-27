package com.plg.orders;

import java.util.List;

final class OrderProcessor {

    private final LineItemValidator validator = new LineItemValidator();
    private final OrderCalculator calculator = new OrderCalculator();
    private final ReceiptFormatter formatter = new ReceiptFormatter();

    OrderResult<String> process(List<LineItem> lineItems) {
        List<String> errors = validator.validate(lineItems);
        if (!errors.isEmpty()) {
            return new OrderResult.Failure<>(errors);
        }
        OrderTotals totals = calculator.calculate(lineItems);
        String receipt = formatter.format(lineItems, totals);
        return new OrderResult.Success<>(receipt);
    }
}
