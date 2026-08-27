package com.example.orders;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LineItemTest {

    @Test
    void computesLineTotalForValidItem() {
        LineItem item = new LineItem("Widget", new BigDecimal("9.99"), 3);

        assertEquals(new BigDecimal("29.97"), item.lineTotal());
    }

    @Test
    void rejectsBlankDescription() {
        assertThrows(InvalidLineItemException.class,
                () -> new LineItem("  ", BigDecimal.TEN, 1));
    }

    @Test
    void rejectsNegativeUnitPrice() {
        assertThrows(InvalidLineItemException.class,
                () -> new LineItem("Widget", new BigDecimal("-1.00"), 1));
    }

    @Test
    void rejectsNonPositiveQuantity() {
        assertThrows(InvalidLineItemException.class,
                () -> new LineItem("Widget", BigDecimal.TEN, 0));
    }
}
