// LineItemTest.java
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class LineItemTest {

    @Test
    void computesLineTotalAsPriceTimesQuantity() {
        LineItem item = new LineItem("Widget", new BigDecimal("2.50"), 3);
        assertEquals(new BigDecimal("7.50"), item.lineTotal());
    }

    @Test
    void rejectsBlankDescription() {
        assertThrows(IllegalArgumentException.class,
                () -> new LineItem("  ", new BigDecimal("1.00"), 1));
    }

    @Test
    void rejectsNegativeUnitPrice() {
        assertThrows(IllegalArgumentException.class,
                () -> new LineItem("Widget", new BigDecimal("-0.01"), 1));
    }

    @Test
    void rejectsZeroOrNegativeQuantity() {
        assertThrows(IllegalArgumentException.class,
                () -> new LineItem("Widget", new BigDecimal("1.00"), 0));
    }
}
