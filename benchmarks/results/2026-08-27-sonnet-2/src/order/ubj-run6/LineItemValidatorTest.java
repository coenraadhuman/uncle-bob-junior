package com.plg.orders;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LineItemValidatorTest {

    private final LineItemValidator validator = new LineItemValidator();

    @Test
    void acceptsAllValidLineItems() {
        List<LineItem> items = List.of(new LineItem("Widget", new BigDecimal("10.00"), 2));

        assertTrue(validator.validate(items).isEmpty());
    }

    @Test
    void rejectsEmptyOrder() {
        List<String> errors = validator.validate(List.of());

        assertEquals(1, errors.size());
    }

    @Test
    void rejectsBlankDescription() {
        List<LineItem> items = List.of(new LineItem(" ", new BigDecimal("10.00"), 1));

        assertEquals(1, validator.validate(items).size());
    }

    @Test
    void rejectsNonPositiveUnitPrice() {
        List<LineItem> items = List.of(new LineItem("Widget", BigDecimal.ZERO, 1));

        assertEquals(1, validator.validate(items).size());
    }

    @Test
    void rejectsNonPositiveQuantity() {
        List<LineItem> items = List.of(new LineItem("Widget", new BigDecimal("10.00"), 0));

        assertEquals(1, validator.validate(items).size());
    }

    @Test
    void collectsMultipleErrorsAcrossLines() {
        List<LineItem> items = List.of(
                new LineItem("", BigDecimal.ZERO, 0),
                new LineItem("Widget", new BigDecimal("10.00"), 1));

        assertEquals(3, validator.validate(items).size());
    }
}
