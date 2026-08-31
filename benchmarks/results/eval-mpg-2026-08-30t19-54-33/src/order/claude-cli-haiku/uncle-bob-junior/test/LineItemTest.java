import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;

class LineItemTest {
    @Test
    void validLineItemCalculatesTotal() {
        LineItem item = new LineItem("Widget", 2, new BigDecimal("50.00"));
        assertEquals("Widget", item.getDescription());
        assertEquals(2, item.getQuantity());
        assertEquals(new BigDecimal("100.00"), item.getLineTotal());
    }

    @Test
    void rejectsEmptyDescription() {
        assertThrows(IllegalArgumentException.class, () ->
            new LineItem("", 1, new BigDecimal("50.00")));
    }

    @Test
    void rejectsZeroOrNegativeQuantity() {
        assertThrows(IllegalArgumentException.class, () ->
            new LineItem("Widget", 0, new BigDecimal("50.00")));
        assertThrows(IllegalArgumentException.class, () ->
            new LineItem("Widget", -1, new BigDecimal("50.00")));
    }

    @Test
    void rejectsNegativePrice() {
        assertThrows(IllegalArgumentException.class, () ->
            new LineItem("Widget", 1, new BigDecimal("-10.00")));
    }
}
