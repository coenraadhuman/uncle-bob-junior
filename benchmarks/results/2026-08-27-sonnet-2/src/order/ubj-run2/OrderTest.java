package com.example.orders;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class OrderTest {

    @Test
    void rejectsEmptyLineItems() {
        assertThrows(IllegalArgumentException.class, () -> new Order(List.of()));
    }

    @Test
    void rejectsNullLineItems() {
        assertThrows(IllegalArgumentException.class, () -> new Order(null));
    }

    @Test
    void lineItemsAreImmutable() {
        List<LineItem> mutable = new ArrayList<>();
        mutable.add(new LineItem("Widget", BigDecimal.ONE, 1));
        Order order = new Order(mutable);

        mutable.add(new LineItem("Gadget", BigDecimal.TEN, 1));

        assertTrue(order.lineItems().size() == 1);
        assertThrows(UnsupportedOperationException.class,
                () -> order.lineItems().add(new LineItem("Gizmo", BigDecimal.ONE, 1)));
    }
}
