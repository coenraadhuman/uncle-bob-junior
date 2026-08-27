import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;

public class OrderTest {
    
    @Test
    void ordersUnder100ComputeVatWithoutDiscount() {
        Order order = new Order();
        order.addItem(new LineItem(1, new BigDecimal("50.00")));
        
        assertEquals(new BigDecimal("50.00"), order.subtotal());
        assertEquals(new BigDecimal("0.00"), order.discountAmount());
        assertEquals(new BigDecimal("10.50"), order.vat());
        assertEquals(new BigDecimal("60.50"), order.total());
    }
    
    @Test
    void ordersExceeding100ApplyDiscount() {
        Order order = new Order();
        order.addItem(new LineItem(2, new BigDecimal("60.00")));
        
        assertEquals(new BigDecimal("120.00"), order.subtotal());
        assertEquals(new BigDecimal("12.00"), order.discountAmount());
        assertEquals(new BigDecimal("108.00"), order.amountAfterDiscount());
        assertEquals(new BigDecimal("22.68"), order.vat());
        assertEquals(new BigDecimal("130.68"), order.total());
    }
    
    @Test
    void exactly100DoesNotQualifyForDiscount() {
        Order order = new Order();
        order.addItem(new LineItem(1, new BigDecimal("100.00")));
        
        assertEquals(new BigDecimal("0.00"), order.discountAmount());
        assertEquals(new BigDecimal("21.00"), order.vat());
        assertEquals(new BigDecimal("121.00"), order.total());
    }
    
    @Test
    void multipleItemsAreIncludedInCalculations() {
        Order order = new Order();
        order.addItem(new LineItem(2, new BigDecimal("40.00")));
        order.addItem(new LineItem(1, new BigDecimal("35.00")));
        
        assertEquals(new BigDecimal("115.00"), order.subtotal());
        assertEquals(new BigDecimal("11.50"), order.discountAmount());
        assertEquals(new BigDecimal("21.74"), order.vat());
        assertEquals(new BigDecimal("125.24"), order.total());
    }
    
    @Test
    void invalidQuantityThrows() {
        assertThrows(IllegalArgumentException.class, 
            () -> new LineItem(0, new BigDecimal("50.00")));
        assertThrows(IllegalArgumentException.class, 
            () -> new LineItem(-5, new BigDecimal("50.00")));
    }
    
    @Test
    void invalidPriceThrows() {
        assertThrows(IllegalArgumentException.class, 
            () -> new LineItem(1, null));
        assertThrows(IllegalArgumentException.class, 
            () -> new LineItem(1, new BigDecimal("-50.00")));
    }
    
    @Test
    void nullItemThrows() {
        Order order = new Order();
        assertThrows(IllegalArgumentException.class, 
            () -> order.addItem(null));
    }
    
    @Test
    void receiptIncludesAllCalculations() {
        Order order = new Order();
        order.addItem(new LineItem(1, new BigDecimal("50.00")));
        
        String receipt = order.receipt();
        assertTrue(receipt.contains("RECEIPT"));
        assertTrue(receipt.contains("Subtotal: €50.00"));
        assertTrue(receipt.contains("VAT (21%): €10.50"));
        assertTrue(receipt.contains("Total: €60.50"));
    }
    
    @Test
    void receiptShowsDiscountWhenApplied() {
        Order order = new Order();
        order.addItem(new LineItem(2, new BigDecimal("60.00")));
        
        String receipt = order.receipt();
        assertTrue(receipt.contains("Discount (10%): -€12.00"));
    }
}
