// filename: LineItemTest.java
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LineItemTest {

    @Test
    void computesLineTotalForValidItem() {
        LineItem item = new LineItem("Widget", 2, new BigDecimal("10.00"));

        assertEquals(new BigDecimal("20.00"), item.lineTotal());
    }

    @Test
    void rejectsBlankProductName() {
        assertThrows(IllegalArgumentException.class,
                () -> new LineItem("  ", 1, new BigDecimal("10.00")));
    }

    @Test
    void rejectsNonPositiveQuantity() {
        assertThrows(IllegalArgumentException.class,
                () -> new LineItem("Widget", 0, new BigDecimal("10.00")));
    }

    @Test
    void rejectsNegativeUnitPrice() {
        assertThrows(IllegalArgumentException.class,
                () -> new LineItem("Widget", 1, new BigDecimal("-1.00")));
    }
}
