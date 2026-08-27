// LineItem.java
package com.example.order;

import java.math.BigDecimal;

public record LineItem(String description, int quantity, BigDecimal unitPrice) {

    public LineItem {
        if (description == null || description.isBlank()) {
            throw new InvalidLineItemException("Description must not be blank");
        }
        if (quantity <= 0) {
            throw new InvalidLineItemException("Quantity must be positive, got " + quantity);
        }
        if (unitPrice == null || unitPrice.signum() <= 0) {
            throw new InvalidLineItemException("Unit price must be positive");
        }
    }

    public BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
