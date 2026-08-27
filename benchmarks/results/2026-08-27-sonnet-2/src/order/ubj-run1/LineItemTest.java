// LineItemTest.java
package com.example.order;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class LineItemTest {

    @Test
    void computesLineTotalAsQuantityTimesUnitPrice() {
        LineItem item = new LineItem("Widget", 3, new BigDecimal("2.50"));
        assertEquals(new BigDecimal("7.50"), item.lineTotal());
    }

    @Test
    void rejectsBlankDescription() {
        assertThrows(InvalidLineItemException.class,
                () -> new LineItem("  ", 1, BigDecimal.ONE));
    }

    @Test
    void rejectsZeroOrNegativeQuantity() {
        assertThrows(InvalidLineItemException.class,
                () -> new LineItem("Widget", 0, BigDecimal.ONE));
        assertThrows(InvalidLineItemException.class,
                () -> new LineItem("Widget", -1, BigDecimal.ONE));
    }

    @Test
    void rejectsZeroOrNegativeUnitPrice() {
        assertThrows(InvalidLineItemException.class,
                () -> new LineItem("Widget", 1, BigDecimal.ZERO));
        assertThrows(InvalidLineItemException.class,
                () -> new LineItem("Widget", 1, new BigDecimal("-1.00")));
    }
}
