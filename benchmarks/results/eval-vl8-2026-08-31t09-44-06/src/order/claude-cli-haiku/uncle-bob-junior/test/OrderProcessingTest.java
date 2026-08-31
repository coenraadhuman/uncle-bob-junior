import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderProcessingTest {
    
    @Test
    void validOrderWithoutDiscount() {
        Order order = new Order(List.of(
            new LineItem("Widget", 2, new BigDecimal("25.00")),
            new LineItem("Gadget", 1, new BigDecimal("30.00"))
        ));
        
        String receipt = order.generateReceipt();
        
        assertTrue(receipt.contains("Widget: €50.00"));
        assertTrue(receipt.contains("Gadget: €30.00"));
        assertTrue(receipt.contains("Subtotal: €80.00"));
        assertFalse(receipt.contains("Discount"));
        assertTrue(receipt.contains("VAT (21%): €16.80"));
        assertTrue(receipt.contains("Total: €96.80"));
    }
    
    @Test
    void discountAppliedWhenSubtotalExceeds100Euros() {
        Order order = new Order(List.of(
            new LineItem("Expensive Item", 5, new BigDecimal("30.00"))
        ));
        
        String receipt = order.generateReceipt();
        
        assertTrue(receipt.contains("Subtotal: €150.00"));
        assertTrue(receipt.contains("Discount (10%): -€15.00"));
        assertTrue(receipt.contains("VAT (21%): €28.35"));
        assertTrue(receipt.contains("Total: €163.35"));
    }
    
    @Test
    void noDiscountAtThreshold() {
        Order order = new Order(List.of(
            new LineItem("Item", 4, new BigDecimal("25.00"))
        ));
        
        String receipt = order.generateReceipt();
        
        assertTrue(receipt.contains("Subtotal: €100.00"));
        assertFalse(receipt.contains("Discount"));
    }
    
    @Test
    void discountAppliedJustAboveThreshold() {
        Order order = new Order(List.of(
            new LineItem("Item", 1, new BigDecimal("100.50"))
        ));
        
        String receipt = order.generateReceipt();
        
        assertTrue(receipt.contains("Discount (10%): -€10.05"));
    }
    
    @Test
    void rejectsEmptyDescription() {
        assertThrows(IllegalArgumentException.class, () ->
            new LineItem("", 1, new BigDecimal("10.00")).validate()
        );
    }
    
    @Test
    void rejectsZeroQuantity() {
        assertThrows(IllegalArgumentException.class, () ->
            new LineItem("Item", 0, new BigDecimal("10.00")).validate()
        );
    }
    
    @Test
    void rejectsNegativePrice() {
        assertThrows(IllegalArgumentException.class, () ->
            new LineItem("Item", 1, new BigDecimal("-10.00")).validate()
        );
    }
    
    @Test
    void rejectsNullDescription() {
        assertThrows(IllegalArgumentException.class, () ->
            new LineItem(null, 1, new BigDecimal("10.00")).validate()
        );
    }
    
    @Test
    void rejectsEmptyOrder() {
        Order order = new Order(List.of());
        assertThrows(IllegalArgumentException.class, order::generateReceipt);
    }
    
    @Test
    void vatRoundedToTwoDenimalPlaces() {
        Order order = new Order(List.of(
            new LineItem("Item", 1, new BigDecimal("10.00"))
        ));
        
        String receipt = order.generateReceipt();
        
        assertTrue(receipt.contains("VAT (21%): €2.10"));
        assertTrue(receipt.contains("Total: €12.10"));
    }
}
