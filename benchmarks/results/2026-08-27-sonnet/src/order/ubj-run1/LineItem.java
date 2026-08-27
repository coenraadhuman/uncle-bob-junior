// LineItem.java
package com.plg.orders;

import java.math.BigDecimal;

public record LineItem(String description, BigDecimal unitPrice, int quantity) {

    public LineItem {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Line item description must not be blank");
        }
        if (unitPrice == null || unitPrice.signum() <= 0) {
            throw new IllegalArgumentException("Unit price must be positive: " + unitPrice);
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive: " + quantity);
        }
    }

    public BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
