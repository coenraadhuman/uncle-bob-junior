package com.example.orders;

import java.math.BigDecimal;

public record LineItem(String description, BigDecimal unitPrice, int quantity) {

    public LineItem {
        if (description == null || description.isBlank()) {
            throw new InvalidLineItemException("Description must not be blank.");
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidLineItemException("Unit price must not be negative.");
        }
        if (quantity <= 0) {
            throw new InvalidLineItemException("Quantity must be positive.");
        }
    }

    BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
