// File: LineItemValidatorTest.java
package com.postcodeloterij.orders;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LineItemValidatorTest {

    private final LineItemValidator validator = new LineItemValidator();

    @Test
    void acceptsAValidLineItem() {
        LineItem item = new LineItem("Widget", 2, new BigDecimal("9.99"));
        assertDoesNotThrow(() -> validator.validate(item));
    }

    @Test
    void rejectsBlankDescription() {
        LineItem item = new LineItem("   ", 1, new BigDecimal("9.99"));
        assertThrows(OrderValidationException.class, () -> validator.validate(item));
    }

    @Test
    void rejectsZeroQuantity() {
        LineItem item = new LineItem("Widget", 0, new BigDecimal("9.99"));
        assertThrows(OrderValidationException.class, () -> validator.validate(item));
    }

    @Test
    void rejectsNegativeQuantity() {
        LineItem item = new LineItem("Widget", -1, new BigDecimal("9.99"));
        assertThrows(OrderValidationException.class, () -> validator.validate(item));
    }

    @Test
    void rejectsNegativeUnitPrice() {
        LineItem item = new LineItem("Widget", 1, new BigDecimal("-0.01"));
        assertThrows(OrderValidationException.class, () -> validator.validate(item));
    }

    @Test
    void rejectsEmptyOrder() {
        assertThrows(OrderValidationException.class, () -> validator.validateAll(List.of()));
    }
}
