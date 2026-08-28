// File: LineItemValidator.java
package com.postcodeloterij.orders;

import java.math.BigDecimal;
import java.util.List;

public final class LineItemValidator {

    private static final int MINIMUM_QUANTITY = 1;
    private static final BigDecimal MINIMUM_UNIT_PRICE = BigDecimal.ZERO;

    public void validateAll(List<LineItem> lineItems) {
        if (lineItems.isEmpty()) {
            throw new OrderValidationException("Order must contain at least one line item");
        }
        lineItems.forEach(this::validate);
    }

    public void validate(LineItem item) {
        if (item.description().isBlank()) {
            throw new OrderValidationException("Line item description must not be blank");
        }
        if (item.quantity() < MINIMUM_QUANTITY) {
            throw new OrderValidationException("Line item quantity must be at least " + MINIMUM_QUANTITY);
        }
        if (item.unitPrice().compareTo(MINIMUM_UNIT_PRICE) < 0) {
            throw new OrderValidationException("Line item unit price must not be negative");
        }
    }
}
