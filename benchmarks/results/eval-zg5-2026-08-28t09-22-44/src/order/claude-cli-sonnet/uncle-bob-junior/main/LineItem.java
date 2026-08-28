// File: LineItem.java
package com.postcodeloterij.orders;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * One item on an order. Amounts are in EUR.
 */
public record LineItem(String description, int quantity, BigDecimal unitPrice) {

    public LineItem {
        Objects.requireNonNull(description, "description must not be null");
        Objects.requireNonNull(unitPrice, "unitPrice must not be null");
    }

    public BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
