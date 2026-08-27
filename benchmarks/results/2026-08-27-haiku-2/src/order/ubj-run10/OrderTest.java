import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class OrderTest {

    @Test
    public void orderWithoutDiscountQualification() {
        List<LineItem> items = List.of(
                new LineItem("Apple", 1.0, 10),
                new LineItem("Banana", 2.0, 5)
        );
        Order order = new Order(items);

        assertEquals(20.0, order.getSubtotalBeforeDiscount());
        assertFalse(order.isEligibleForDiscount());
        assertEquals(0.0, order.getDiscountAmount());
        assertEquals(20.0, order.getSubtotalAfterDiscount());
        assertEquals(4.2, order.getVatAmount(), 0.01);
        assertEquals(24.2, order.getTotalWithVat(), 0.01);
    }

    @Test
    public void orderExceedingDiscountThresholdApplies10Percent() {
        List<LineItem> items = List.of(
                new LineItem("Laptop", 60.0, 2),
                new LineItem("Mouse", 20.0, 1)
        );
        Order order = new Order(items);

        assertEquals(140.0, order.getSubtotalBeforeDiscount());
        assertTrue(order.isEligibleForDiscount());
        assertEquals(14.0, order.getDiscountAmount(), 0.01);
        assertEquals(126.0, order.getSubtotalAfterDiscount(), 0.01);
        assertEquals(26.46, order.getVatAmount(), 0.01);
        assertEquals(152.46, order.getTotalWithVat(), 0.01);
    }

    @Test
    public void receiptIncludesLineItemsAndTotals() {
        List<LineItem> items = List.of(
                new LineItem("Book", 50.0, 3)
        );
        Order order = new Order(items);
        String receipt = order.generateReceipt();

        assertTrue(receipt.contains("Book x3 @ €50.00 = €150.00"));
        assertTrue(receipt.contains("Discount 10%: -€15.00"));
        assertTrue(receipt.contains("TOTAL:        €165.90"));
    }

    @Test
    public void lineItemValidatesNonEmptyName() {
        assertThrows(IllegalArgumentException.class, 
                () -> new LineItem("", 10.0, 1));
    }

    @Test
    public void lineItemValidatesNonNegativePrice() {
        assertThrows(IllegalArgumentException.class, 
                () -> new LineItem("Product", -5.0, 1));
    }

    @Test
    public void lineItemValidatesPositiveQuantity() {
        assertThrows(IllegalArgumentException.class, 
                () -> new LineItem("Product", 10.0, 0));
    }

    @Test
    public void orderRequiresAtLeastOneLineItem() {
        assertThrows(IllegalArgumentException.class, 
                () -> new Order(List.of()));
    }
}
