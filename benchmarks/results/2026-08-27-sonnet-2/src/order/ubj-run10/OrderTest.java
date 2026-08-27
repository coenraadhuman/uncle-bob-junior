// OrderTest.java
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    @Test
    void rejectsEmptyItemList() {
        assertThrows(IllegalArgumentException.class, () -> new Order(List.of()));
    }

    @Test
    void rejectsNullItemList() {
        assertThrows(NullPointerException.class, () -> new Order(null));
    }

    @Test
    void acceptsOneOrMoreValidItems() {
        LineItem item = new LineItem("Widget", new BigDecimal("10.00"), 1);
        Order order = new Order(List.of(item));
        assertEquals(1, order.items().size());
    }
}
