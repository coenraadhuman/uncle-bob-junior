import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {
    
    @Test
    void validateRejectsZeroQuantity() {
        var order = new Order(List.of(new LineItem("Item", 0, BigDecimal.TEN)));
        assertThrows(IllegalArgumentException.class, order::validate);
    }
    
    @Test
    void validateRejectsNegativePrice() {
        var order = new Order(List.of(new LineItem("Item", 1, BigDecimal.valueOf(-10))));
        assertThrows(IllegalArgumentException.class, order::validate);
    }
    
    @Test
    void validateAcceptsValidItems() {
        var order = new Order(List.of(new LineItem("Item", 1, BigDecimal.TEN)));
        order.validate();
    }
    
    @Test
    void subtotalSumsLineItems() {
        var items = List.of(
            new LineItem("A", 2, BigDecimal.TEN),
            new LineItem("B", 3, BigDecimal.valueOf(5))
        );
        assertEquals(BigDecimal.valueOf(35), new Order(items).subtotal());
    }
    
    @Test
    void discountAppliesWhenSubtotalExceeds100() {
        var order = new Order(List.of(new LineItem("Item", 11, BigDecimal.TEN)));
        assertEquals(new BigDecimal("11.00"), order.discountAmount());
    }
    
    @Test
    void discountDoesNotApplyWhen100OrLess() {
        var order = new Order(List.of(new LineItem("Item", 10, BigDecimal.TEN)));
        assertEquals(BigDecimal.ZERO, order.discountAmount());
    }
    
    @Test
    void totalWithoutDiscount() {
        var order = new Order(List.of(new LineItem("Item", 10, BigDecimal.TEN)));
        assertEquals(new BigDecimal("12.10"), order.total());
    }
    
    @Test
    void totalWithDiscount() {
        var order = new Order(List.of(new LineItem("Item", 11, BigDecimal.TEN)));
        assertEquals(new BigDecimal("119.79"), order.total());
    }
    
    @Test
    void receiptIncludesLineItems() {
        var receipt = new Order(List.of(new LineItem("Coffee", 2, BigDecimal.valueOf(5)))).receipt();
        assertTrue(receipt.contains("Coffee") && receipt.contains("10.00"));
    }
    
    @Test
    void receiptIncludesDiscount() {
        var receipt = new Order(List.of(new LineItem("Item", 11, BigDecimal.TEN))).receipt();
        assertTrue(receipt.contains("Discount (10%)") && receipt.contains("11.00"));
    }
}
