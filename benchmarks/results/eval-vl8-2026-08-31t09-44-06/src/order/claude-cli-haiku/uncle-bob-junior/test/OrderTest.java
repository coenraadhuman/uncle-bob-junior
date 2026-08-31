import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class OrderTest {
    @Test
    void createsValidOrder() {
        List<LineItem> items = List.of(
            new LineItem("Item A", 1, new BigDecimal("50.00")),
            new LineItem("Item B", 2, new BigDecimal("30.00"))
        );
        Order order = new Order(items);
        assertEquals(2, order.items().size());
    }
    
    @Test
    void rejectsEmptyOrder() {
        assertThrows(IllegalArgumentException.class, () -> new Order(List.of()));
    }
    
    @Test
    void itemsAreImmutable() {
        List<LineItem> items = new ArrayList<>(List.of(
            new LineItem("Item A", 1, new BigDecimal("50.00"))
        ));
        Order order = new Order(items);
        items.add(new LineItem("Item B", 1, new BigDecimal("30.00")));
        assertEquals(1, order.items().size());
    }
}
