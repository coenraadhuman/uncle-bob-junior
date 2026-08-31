package com.plg.orders;

import java.math.BigDecimal;
import java.util.Objects;

public record LineItem(String description, BigDecimal unitPrice, int quantity) {

    public LineItem {
        Objects.requireNonNull(unitPrice, "unitPrice must not be null");
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("description must not be blank");
        }
        if (unitPrice.signum() <= 0) {
            throw new IllegalArgumentException("unitPrice must be positive");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
    }

    public BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
