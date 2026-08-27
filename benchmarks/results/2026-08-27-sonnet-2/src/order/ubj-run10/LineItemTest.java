// LineItemTest.java
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class LineItemTest {

    @Test
    void computesLineTotalAsPriceTimesQuantity() {
        LineItem item = new LineItem("Widget", new BigDecimal("10.00"), 3);
        assertEquals(new BigDecimal("30.00"), item.lineTotal());
    }

    @Test
    void rejectsBlankDescription() {
        assertThrows(IllegalArgumentException.class,
                () -> new LineItem("  ", new BigDecimal("10.00"), 1));
    }

    @Test
    void rejectsZeroOrNegativeUnitPrice() {
        assertThrows(IllegalArgumentException.class,
                () -> new LineItem("Widget", BigDecimal.ZERO, 1));
        assertThrows(IllegalArgumentException.class,
                () -> new LineItem("Widget", new BigDecimal("-1.00"), 1));
    }

    @Test
    void rejectsZeroOrNegativeQuantity() {
        assertThrows(IllegalArgumentException.class,
                () -> new LineItem("Widget", new BigDecimal("10.00"), 0));
    }
}
