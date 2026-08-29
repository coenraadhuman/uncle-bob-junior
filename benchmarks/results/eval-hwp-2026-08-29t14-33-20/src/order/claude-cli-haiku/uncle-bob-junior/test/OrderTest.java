import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class OrderTest {
    @Test
    void rejectsEmptyOrder() {
        assertThrows(IllegalArgumentException.class, () -> new Order(List.of()));
    }

    @Test
    void rejectsZeroQuantity() {
        assertThrows(IllegalArgumentException.class, () -> 
            new Order(List.of(new LineItem("Item", 0, new BigDecimal("10.00")))));
    }

    @Test
    void rejectsNegativePrice() {
        assertThrows(IllegalArgumentException.class, () -> 
            new Order(List.of(new LineItem("Item", 1, new BigDecimal("-5.00")))));
    }

    @Test
    void computesTotalWithoutDiscount() {
        Order order = new Order(List.of(
            new LineItem("Apple", 5, new BigDecimal("10.00"))
        ));
        
        String receipt = order.generateReceipt();
        
        assertTrue(receipt.contains("Subtotal: €50.00"));
        assertFalse(receipt.contains("Discount"));
        assertTrue(receipt.contains("VAT (21%): €10.50"));
        assertTrue(receipt.contains("Total: €60.50"));
    }

    @Test
    void appliesDiscountWhenSubtotalExceeds100() {
        Order order = new Order(List.of(
            new LineItem("Item A", 5, new BigDecimal("25.00")),
            new LineItem("Item B", 3, new BigDecimal("10.00"))
        ));
        
        String receipt = order.generateReceipt();
        
        assertTrue(receipt.contains("Subtotal: €155.00"));
        assertTrue(receipt.contains("Discount (10%): -€15.50"));
        assertTrue(receipt.contains("VAT (21%): €29.30"));
        assertTrue(receipt.contains("Total: €168.80"));
    }

    @Test
    void doesNotApplyDiscountAt100() {
        Order order = new Order(List.of(
            new LineItem("Item", 10, new BigDecimal("10.00"))
        ));
        
        String receipt = order.generateReceipt();
        
        assertTrue(receipt.contains("Subtotal: €100.00"));
        assertFalse(receipt.contains("Discount"));
        assertTrue(receipt.contains("VAT (21%): €21.00"));
        assertTrue(receipt.contains("Total: €121.00"));
    }
}
