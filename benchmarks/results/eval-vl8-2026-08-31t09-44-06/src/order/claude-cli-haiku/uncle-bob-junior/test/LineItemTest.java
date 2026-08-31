import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class LineItemTest {
    @Test
    void createsValidLineItem() {
        LineItem item = new LineItem("Widget", 5, new BigDecimal("10.00"));
        assertEquals("Widget", item.description());
        assertEquals(5, item.quantity());
        assertEquals(new BigDecimal("10.00"), item.unitPrice());
    }
    
    @Test
    void rejectsBlankDescription() {
        assertThrows(IllegalArgumentException.class,
            () -> new LineItem("", 1, new BigDecimal("10.00")));
    }
    
    @Test
    void rejectsNonPositiveQuantity() {
        assertThrows(IllegalArgumentException.class,
            () -> new LineItem("Widget", 0, new BigDecimal("10.00")));
    }
    
    @Test
    void rejectsNonPositivePrice() {
        assertThrows(IllegalArgumentException.class,
            () -> new LineItem("Widget", 1, BigDecimal.ZERO));
    }
    
    @Test
    void computesLineTotal() {
        LineItem item = new LineItem("Widget", 5, new BigDecimal("10.50"));
        assertEquals(new BigDecimal("52.50"), item.lineTotal());
    }
}
