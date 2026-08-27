package com.plg.orders;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

final class LineItemValidator {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    List<String> validate(List<LineItem> lineItems) {
        if (lineItems.isEmpty()) {
            return List.of("Order must contain at least one line item");
        }
        List<String> errors = new ArrayList<>();
        for (int i = 0; i < lineItems.size(); i++) {
            errors.addAll(validateLineItem(lineItems.get(i), i));
        }
        return errors;
    }

    private List<String> validateLineItem(LineItem item, int index) {
        List<String> errors = new ArrayList<>();
        if (item.description() == null || item.description().isBlank()) {
            errors.add("Line item %d: description must not be blank".formatted(index));
        }
        if (item.unitPrice() == null || item.unitPrice().compareTo(ZERO) <= 0) {
            errors.add("Line item %d: unit price must be positive".formatted(index));
        }
        if (item.quantity() <= 0) {
            errors.add("Line item %d: quantity must be positive".formatted(index));
        }
        return errors;
    }
}
