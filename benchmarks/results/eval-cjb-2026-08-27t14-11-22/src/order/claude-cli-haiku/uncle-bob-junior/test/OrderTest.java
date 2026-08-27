import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class OrderTest {
    
    @Test
    public void validLineItemCreatesSuccessfully() {
        LineItem item = new LineItem("Widget", new BigDecimal("50.00"), 1);
        assertEquals("Widget", item.getDescription());
        assertEquals(new BigDecimal("50.00"), item.getPrice());
        assertEquals(1, item.getQuantity());
    }
    
    @Test
    public void lineItemSubtotalCalculation() {
        LineItem item = new LineItem("Widget", new BigDecimal("10.00"), 3);
        assertEquals(new BigDecimal("30.00"), item.getSubtotal());
    }
    
    @Test
    public void rejectsNullDescription() {
        assertThrows(IllegalArgumentException.class, () ->
            new LineItem(null, new BigDecimal("10.00"), 1));
    }
    
    @Test
    public void rejectsBlankDescription() {
        assertThrows(IllegalArgumentException.class, () ->
            new LineItem("   ", new BigDecimal("10.00"), 1));
    }
    
    @Test
    public void rejectsZeroOrNegativePrice() {
        assertThrows(IllegalArgumentException.class, () ->
            new LineItem("Widget", BigDecimal.ZERO, 1));
        assertThrows(IllegalArgumentException.class, () ->
            new LineItem("Widget", new BigDecimal("-10.00"), 1));
    }
    
    @Test
    public void rejectsZeroOrNegativeQuantity() {
        assertThrows(IllegalArgumentException.class, () ->
            new LineItem("Widget", new BigDecimal("10.00"), 0));
        assertThrows(IllegalArgumentException.class, () ->
            new LineItem("Widget", new BigDecimal("10.00"), -1));
    }
    
    @Test
    public void rejectsEmptyOrder() {
        assertThrows(IllegalArgumentException.class, () -> new Order(null));
        assertThrows(IllegalArgumentException.class, () -> new Order(List.of()));
    }
    
    @Test
    public void orderWithoutDiscountWhenSubtotalBelowThreshold() {
        Order order = new Order(List.of(
            new LineItem("Widget A", new BigDecimal("40.00"), 1),
            new LineItem("Widget B", new BigDecimal("50.00"), 1)
        ));
        
        String receipt = order.generateReceipt();
        assertTrue(receipt.contains("Subtotal: €90.00"));
        assertFalse(receipt.contains("Discount"));
        assertTrue(receipt.contains("VAT (21%): €18.90"));
        assertTrue(receipt.contains("TOTAL: €108.90"));
    }
    
    @Test
    public void orderWithDiscountWhenSubtotalExceeds100() {
        Order order = new Order(List.of(
            new LineItem("Widget A", new BigDecimal("60.00"), 1),
            new LineItem("Widget B", new BigDecimal("50.00"), 1)
        ));
        
        String receipt = order.generateReceipt();
        assertTrue(receipt.contains("Subtotal: €110.00"));
        assertTrue(receipt.contains("Discount (10%): -€11.00"));
        assertTrue(receipt.contains("Subtotal after discount: €99.00"));
        assertTrue(receipt.contains("VAT (21%): €20.79"));
        assertTrue(receipt.contains("TOTAL: €119.79"));
    }
    
    @Test
    public void receiptIncludesLineItemDetails() {
        Order order = new Order(List.of(
            new LineItem("Widget", new BigDecimal("25.00"), 2)
        ));
        
        String receipt = order.generateReceipt();
        assertTrue(receipt.contains("Widget x2 @ €25.00 = €50.00"));
    }
    
    @Test
    public void discountAppliedExactlyAt100EuroThreshold() {
        Order order = new Order(List.of(
            new LineItem("Item", new BigDecimal("100.00"), 1)
        ));
        
        String receipt = order.generateReceipt();
        assertFalse(receipt.contains("Discount"), "No discount at exactly €100.00");
        assertTrue(receipt.contains("TOTAL: €121.00"));
    }
    
    @Test
    public void discountAppliedJustAbove100EuroThreshold() {
        Order order = new Order(List.of(
            new LineItem("Item", new BigDecimal("100.01"), 1)
        ));
        
        String receipt = order.generateReceipt();
        assertTrue(receipt.contains("Discount (10%): -€10.00"));
        assertTrue(receipt.contains("TOTAL: €119.31"));
    }
}
