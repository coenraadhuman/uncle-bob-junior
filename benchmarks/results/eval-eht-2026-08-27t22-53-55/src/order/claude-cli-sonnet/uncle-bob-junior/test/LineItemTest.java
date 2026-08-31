package com.plg.orders;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class LineItemTest {

    @Test
    void createsValidLineItem() {
        LineItem item = new LineItem("Widget", new BigDecimal("10.00"), 3);
        assertEquals(new BigDecimal("30.00"), item.lineTotal());
    }

    @Test
    void rejectsBlankDescription() {
        assertThrows(IllegalArgumentException.class,
                () -> new LineItem(" ", new BigDecimal("10.00"), 1));
    }

    @Test
    void rejectsNonPositiveUnitPrice() {
        assertThrows(IllegalArgumentException.class,
                () -> new LineItem("Widget", BigDecimal.ZERO, 1));
    }

    @Test
    void rejectsNonPositiveQuantity() {
        assertThrows(IllegalArgumentException.class,
                () -> new LineItem("Widget", new BigDecimal("10.00"), 0));
    }
}
