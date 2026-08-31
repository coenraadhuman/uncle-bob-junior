import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class OrderProcessorTest {
    @Test
    void processesOrderBelowDiscountThreshold() {
        Order order = new Order(List.of(
            new LineItem("Widget", 2, new BigDecimal("40.00"))
        ));
        Receipt receipt = new OrderProcessor().process(order);
        
        String formatted = receipt.format();
        assertTrue(formatted.contains("€80.00"));
        assertFalse(formatted.contains("Discount"));
    }
    
    @Test
    void appliesDiscountWhenAboveThreshold() {
        Order order = new Order(List.of(
            new LineItem("Widget", 3, new BigDecimal("40.00")),
            new LineItem("Gadget", 1, new BigDecimal("20.00"))
        ));
        Receipt receipt = new OrderProcessor().process(order);
        
        String formatted = receipt.format();
        assertTrue(formatted.contains("Discount (10%)"));
    }
    
    @Test
    void appliesVatCorrectly() {
        Order order = new Order(List.of(
            new LineItem("Item", 1, new BigDecimal("100.00"))
        ));
        Receipt receipt = new OrderProcessor().process(order);
        
        String formatted = receipt.format();
        assertTrue(formatted.contains("€21.00")); // 21% VAT
        assertTrue(formatted.contains("€121.00")); // Total
    }
    
    @Test
    void computesComplexOrderWithDiscount() {
        Order order = new Order(List.of(
            new LineItem("Product A", 2, new BigDecimal("35.00")),
            new LineItem("Product B", 1, new BigDecimal("50.00")),
            new LineItem("Product C", 3, new BigDecimal("15.00"))
        ));
        Receipt receipt = new OrderProcessor().process(order);
        
        String formatted = receipt.format();
        assertTrue(formatted.contains("Subtotal: €       155.00"));
        assertTrue(formatted.contains("Discount (10%): -€     15.50"));
        assertTrue(formatted.contains("TOTAL:"));
    }
}
