// OrderLineItemTest.java
package com.plg.order;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderLineItemTest {

    @Test
    void computesLineTotalAsPriceTimesQuantity() {
        OrderLineItem item = new OrderLineItem("Widget", new BigDecimal("9.99"), 3);

        assertEquals(new BigDecimal("29.97"), item.lineTotal());
    }

    @Test
    void rejectsBlankDescription() {
        assertThrows(IllegalArgumentException.class,
                () -> new OrderLineItem(" ", BigDecimal.TEN, 1));
    }

    @Test
    void rejectsNonPositiveUnitPrice() {
        assertThrows(IllegalArgumentException.class,
                () -> new OrderLineItem("Widget", BigDecimal.ZERO, 1));
    }

    @Test
    void rejectsNonPositiveQuantity() {
        assertThrows(IllegalArgumentException.class,
                () -> new OrderLineItem("Widget", BigDecimal.TEN, 0));
    }
}
