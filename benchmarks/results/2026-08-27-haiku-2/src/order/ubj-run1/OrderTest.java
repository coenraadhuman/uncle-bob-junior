import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class OrderTest {

    @Test
    public void lineItemRejectsBlankDescription() {
        assertThrows(IllegalArgumentException.class, 
            () -> new LineItem("", new BigDecimal("10"), 1));
    }

    @Test
    public void lineItemRejectsNegativePrice() {
        assertThrows(IllegalArgumentException.class, 
            () -> new LineItem("Item", new BigDecimal("-5"), 1));
    }

    @Test
    public void lineItemRejectsZeroQuantity() {
        assertThrows(IllegalArgumentException.class, 
            () -> new LineItem("Item", new BigDecimal("10"), 0));
    }

    @Test
    public void lineItemComputesSubtotal() {
        LineItem item = new LineItem("Widget", new BigDecimal("25.00"), 4);
        assertEquals(new BigDecimal("100.00"), item.getSubtotal());
    }

    @Test
    public void orderWithoutDiscountIncludesVatOnly() {
        Order order = new Order();
        order.addLineItem(new LineItem("Item A", new BigDecimal("30.00"), 1));
        order.addLineItem(new LineItem("Item B", new BigDecimal("40.00"), 1));

        String receipt = order.generateReceipt();
        assertTrue(receipt.contains("Pre-tax total:  €70.00"));
        assertTrue(receipt.contains("VAT (21%):     €14.70"));
        assertTrue(receipt.contains("Total:          €84.70"));
        assertFalse(receipt.contains("Discount"));
    }

    @Test
    public void orderExceedingThresholdAppliesDiscount() {
        Order order = new Order();
        order.addLineItem(new LineItem("Item A", new BigDecimal("60.00"), 1));
        order.addLineItem(new LineItem("Item B", new BigDecimal("50.00"), 1));

        String receipt = order.generateReceipt();
        assertTrue(receipt.contains("Pre-tax total:  €110.00"));
        assertTrue(receipt.contains("Discount (10%): -€11.00"));
        assertTrue(receipt.contains("VAT (21%):     €20.79"));
        assertTrue(receipt.contains("Total:          €119.79"));
    }

    @Test
    public void emptyOrderThrows() {
        Order order = new Order();
        assertThrows(IllegalStateException.class, order::generateReceipt);
    }
}
