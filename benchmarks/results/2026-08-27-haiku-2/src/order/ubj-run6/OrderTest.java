import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OrderTest {
    @Test
    void rejectsInvalidLineItems() {
        assertThrows(IllegalArgumentException.class, 
            () -> new LineItem("Widget", 0, BigDecimal.TEN));
        assertThrows(IllegalArgumentException.class, 
            () -> new LineItem("Widget", 1, BigDecimal.ZERO));
    }
    
    @Test
    void rejectsEmptyOrder() {
        assertThrows(IllegalArgumentException.class, 
            () -> new Order(List.of()));
    }
    
    @Test
    void computesTotalWithoutDiscount() {
        Order order = new Order(List.of(
            new LineItem("Widget", 2, new BigDecimal("25.00"))
        ));
        // 50.00 + (50.00 * 0.21) = 60.50
        assertEquals(new BigDecimal("60.50"), order.grandTotal());
    }
    
    @Test
    void appliesDiscountWhenThresholdExceeded() {
        Order order = new Order(List.of(
            new LineItem("Widget", 5, new BigDecimal("25.00"))
        ));
        // Pre-VAT: 125 → discount: 12.50 → after: 112.50 → VAT: 23.63 → total: 136.13
        assertEquals(new BigDecimal("12.50"), order.discountAmount());
        assertEquals(new BigDecimal("136.13"), order.grandTotal());
    }
    
    @Test
    void noDiscountWhenBelowThreshold() {
        Order order = new Order(List.of(
            new LineItem("Item", 2, new BigDecimal("40.00"))
        ));
        assertEquals(BigDecimal.ZERO, order.discountAmount());
    }
    
    @Test
    void receiptContainsAllComponents() {
        Order order = new Order(List.of(
            new LineItem("Widget", 1, new BigDecimal("150.00"))
        ));
        String receipt = order.receipt();
        assertTrue(receipt.contains("Widget"));
        assertTrue(receipt.contains("Subtotal"));
        assertTrue(receipt.contains("Discount"));
        assertTrue(receipt.contains("VAT"));
        assertTrue(receipt.contains("TOTAL"));
    }
}
