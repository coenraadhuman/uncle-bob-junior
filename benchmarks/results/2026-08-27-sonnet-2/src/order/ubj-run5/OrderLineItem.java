// OrderLineItem.java
package com.plg.order;

import java.math.BigDecimal;

public record OrderLineItem(String description, BigDecimal unitPrice, int quantity) {

    public OrderLineItem {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Line item description must not be blank");
        }
        if (unitPrice == null || unitPrice.signum() <= 0) {
            throw new IllegalArgumentException("Unit price must be positive for: " + description);
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive for: " + description);
        }
    }

    public BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
