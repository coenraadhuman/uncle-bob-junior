import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class OrderTest {
    
    @Test
    void validateRejectsNullLineItems() {
        assertThrows(IllegalArgumentException.class, () -> new Order(null));
    }
    
    @Test
    void validateRejectsEmptyLineItems() {
        assertThrows(IllegalArgumentException.class, () -> new Order(new ArrayList<>()));
    }
    
    @Test
    void validateRejectsNullLineItem() {
        List<LineItem> items = new ArrayList<>();
        items.add(null);
        assertThrows(IllegalArgumentException.class, () -> new Order(items));
    }
    
    @Test
    void validateRejectsNegativeQuantity() {
        List<LineItem> items = List.of(new LineItem("Widget", -1, 10.0));
        assertThrows(IllegalArgumentException.class, () -> new Order(items));
    }
    
    @Test
    void validateRejectsZeroQuantity() {
        List<LineItem> items = List.of(new LineItem("Widget", 0, 10.0));
        assertThrows(IllegalArgumentException.class, () -> new Order(items));
    }
    
    @Test
    void validateRejectsNegativePrice() {
        List<LineItem> items = List.of(new LineItem("Widget", 1, -10.0));
        assertThrows(IllegalArgumentException.class, () -> new Order(items));
    }
    
    @Test
    void receiptWithoutDiscount() {
        List<LineItem> items = List.of(
            new LineItem("Apple", 10, 5.00),
            new LineItem("Orange", 5, 3.00)
        );
        Order order = new Order(items);
        String receipt = order.generateReceipt();
        
        assertTrue(receipt.contains("Subtotal: €65.00"));
        assertTrue(receipt.contains("VAT (21%): €13.65"));
        assertTrue(receipt.contains("TOTAL: €78.65"));
        assertFalse(receipt.contains("Discount (10%)"));
    }
    
    @Test
    void receiptWithDiscount() {
        List<LineItem> items = List.of(
            new LineItem("Widget", 20, 6.00)
        );
        Order order = new Order(items);
        String receipt = order.generateReceipt();
        
        assertTrue(receipt.contains("Subtotal: €120.00"));
        assertTrue(receipt.contains("Discount (10%): -€12.00"));
        assertTrue(receipt.contains("Amount after discount: €108.00"));
        assertTrue(receipt.contains("VAT (21%): €22.68"));
        assertTrue(receipt.contains("TOTAL: €130.68"));
    }
    
    @Test
    void noDiscountAtExactThreshold() {
        List<LineItem> items = List.of(
            new LineItem("Item", 100, 1.00)
        );
        Order order = new Order(items);
        String receipt = order.generateReceipt();
        
        assertTrue(receipt.contains("Subtotal: €100.00"));
        assertFalse(receipt.contains("Discount (10%)"));
    }
    
    @Test
    void discountAppliesAboveThreshold() {
        List<LineItem> items = List.of(
            new LineItem("Item", 101, 1.00)
        );
        Order order = new Order(items);
        String receipt = order.generateReceipt();
        
        assertTrue(receipt.contains("Subtotal: €101.00"));
        assertTrue(receipt.contains("Discount (10%): -€10.10"));
    }
}
