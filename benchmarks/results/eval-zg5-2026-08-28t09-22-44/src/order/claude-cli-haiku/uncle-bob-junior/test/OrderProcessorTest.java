import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class OrderProcessorTest {
    
    @Test
    public void lineItemValidatesPositiveQuantity() {
        assertThrows(IllegalArgumentException.class, () ->
            new LineItem("Widget", 0, new BigDecimal("10.00"))
        );
    }
    
    @Test
    public void lineItemValidatesPositivePrice() {
        assertThrows(IllegalArgumentException.class, () ->
            new LineItem("Widget", 1, new BigDecimal("0.00"))
        );
    }
    
    @Test
    public void lineItemCalculatesTotal() {
        LineItem item = new LineItem("Widget", 3, new BigDecimal("10.00"));
        assertEquals(new BigDecimal("30.00"), item.lineTotal());
    }
    
    @Test
    public void orderRequiresAtLeastOneItem() {
        assertThrows(IllegalArgumentException.class, () ->
            new Order(Arrays.asList())
        );
    }
    
    @Test
    public void processOrderWithNoDiscount() {
        List<LineItem> items = Arrays.asList(
            new LineItem("Widget", 5, new BigDecimal("10.00")),
            new LineItem("Gadget", 2, new BigDecimal("15.00"))
        );
        Order order = new Order(items);
        OrderReceipt receipt = new OrderProcessor().process(order);
        
        assertEquals(new BigDecimal("80.00"), receipt.getSubtotal());
        assertEquals(BigDecimal.ZERO, receipt.getDiscount());
        assertEquals(new BigDecimal("16.80"), receipt.getVatAmount());
        assertEquals(new BigDecimal("96.80"), receipt.getTotal());
    }
    
    @Test
    public void processOrderWithDiscount() {
        List<LineItem> items = Arrays.asList(
            new LineItem("Widget", 10, new BigDecimal("10.00")),
            new LineItem("Gadget", 5, new BigDecimal("5.00"))
        );
        Order order = new Order(items);
        OrderReceipt receipt = new OrderProcessor().process(order);
        
        assertEquals(new BigDecimal("125.00"), receipt.getSubtotal());
        assertEquals(new BigDecimal("12.50"), receipt.getDiscount());
        assertEquals(new BigDecimal("23.63"), receipt.getVatAmount());
        assertEquals(new BigDecimal("136.13"), receipt.getTotal());
    }
    
    @Test
    public void processOrderAtDiscountThreshold() {
        List<LineItem> items = Arrays.asList(
            new LineItem("Widget", 10, new BigDecimal("10.00"))
        );
        Order order = new Order(items);
        OrderReceipt receipt = new OrderProcessor().process(order);
        
        assertEquals(new BigDecimal("100.00"), receipt.getSubtotal());
        assertEquals(BigDecimal.ZERO, receipt.getDiscount());
        assertEquals(new BigDecimal("21.00"), receipt.getVatAmount());
        assertEquals(new BigDecimal("121.00"), receipt.getTotal());
    }
    
    @Test
    public void processOrderJustAboveThreshold() {
        List<LineItem> items = Arrays.asList(
            new LineItem("Widget", 101, new BigDecimal("1.00"))
        );
        Order order = new Order(items);
        OrderReceipt receipt = new OrderProcessor().process(order);
        
        assertEquals(new BigDecimal("101.00"), receipt.getSubtotal());
        assertEquals(new BigDecimal("10.10"), receipt.getDiscount());
        assertEquals(new BigDecimal("19.09"), receipt.getVatAmount());
        assertEquals(new BigDecimal("109.99"), receipt.getTotal());
    }
    
    @Test
    public void receiptStringFormattingWithDiscount() {
        List<LineItem> items = Arrays.asList(
            new LineItem("Widget", 6, new BigDecimal("20.00"))
        );
        Order order = new Order(items);
        String receiptString = new OrderProcessor().process(order).toString();
        
        assertTrue(receiptString.contains("Widget"));
        assertTrue(receiptString.contains("120.00"));
        assertTrue(receiptString.contains("Discount"));
    }
    
    @Test
    public void receiptStringFormattingWithoutDiscount() {
        List<LineItem> items = Arrays.asList(
            new LineItem("Widget", 2, new BigDecimal("50.00"))
        );
        Order order = new Order(items);
        String receiptString = new OrderProcessor().process(order).toString();
        
        assertTrue(receiptString.contains("100.00"));
        assertFalse(receiptString.contains("Discount"));
    }
}
