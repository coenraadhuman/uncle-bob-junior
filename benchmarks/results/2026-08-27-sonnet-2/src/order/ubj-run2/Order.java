package com.example.orders;

import java.util.List;

/**
 * An order to process. Invariant: contains at least one line item.
 */
public record Order(List<LineItem> lineItems) {

    public Order {
        if (lineItems == null || lineItems.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one line item");
        }
        lineItems = List.copyOf(lineItems);
    }
}
