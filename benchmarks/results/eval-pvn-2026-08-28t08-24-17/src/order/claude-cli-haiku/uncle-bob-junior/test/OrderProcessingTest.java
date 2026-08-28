import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class OrderProcessingTest {

    @Test
    public void lineItemValidatesNonNullDescription() {
        assertThrows(NullPointerException.class, () ->
            new LineItem(null, 1, new BigDecimal("10")));
    }

    @Test
    public void lineItemValidatesNonNullPrice() {
        assertThrows(NullPointerException.class, () ->
            new LineItem("item", 1, null));
    }

    @Test
    public void lineItemValidatesPositiveQuantity() {
        assertThrows(IllegalArgumentException.class, () ->
            new LineItem("item", 0, new BigDecimal("10")));
        assertThrows(IllegalArgumentException.class, () ->
            new LineItem("item", -1, new BigDecimal("10")));
    }

    @Test
    public void lineItemValidatesNonNegativePrice() {
        assertThrows(IllegalArgumentException.class, () ->
            new LineItem("item", 1, new BigDecimal("-10")));
    }

    @Test
    public void lineItemCalculatesSubtotal() {
        LineItem item = new LineItem("item", 5, new BigDecimal("20"));
        assertEquals(new BigDecimal("100"), item.subtotal());
    }

    @Test
    public void orderRequiresNonEmptyLineItems() {
        assertThrows(IllegalArgumentException.class, () ->
            new Order(new ArrayList<>()));
    }

    @Test
    public void noDiscountWhenSubtotalAtThreshold() {
        Order order = new Order(Arrays.asList(
            new LineItem("item", 1, new BigDecimal("100"))
        ));
        String receipt = order.generateReceipt().toString();

        assertFalse(receipt.contains("Discount"));
        assertTrue(receipt.contains("Subtotal: €100.00"));
        assertTrue(receipt.contains("TOTAL: €121.00"));
    }

    @Test
    public void appliesDiscountWhenSubtotalExceeds100() {
        Order order = new Order(Arrays.asList(
            new LineItem("item", 1, new BigDecimal("150"))
        ));
        String receipt = order.generateReceipt().toString();

        assertTrue(receipt.contains("Discount (10%): -€15.00"));
        assertTrue(receipt.contains("After discount: €135.00"));
        assertTrue(receipt.contains("VAT (21%): €28.35"));
        assertTrue(receipt.contains("TOTAL: €163.35"));
    }

    @Test
    public void calculatesVatOnDiscountedAmount() {
        Order order = new Order(Arrays.asList(
            new LineItem("coffee", 10, new BigDecimal("15"))
        ));
        String receipt = order.generateReceipt().toString();

        assertTrue(receipt.contains("Discount (10%): -€15.00"));
        assertTrue(receipt.contains("After discount: €135.00"));
        assertTrue(receipt.contains("VAT (21%): €28.35"));
    }

    @Test
    public void handlesMultipleLineItems() {
        Order order = new Order(Arrays.asList(
            new LineItem("coffee", 2, new BigDecimal("5.50")),
            new LineItem("pastry", 1, new BigDecimal("3.00"))
        ));
        String receipt = order.generateReceipt().toString();

        assertTrue(receipt.contains("coffee x2 @ €5.50 = €11.00"));
        assertTrue(receipt.contains("pastry x1 @ €3.00 = €3.00"));
        assertTrue(receipt.contains("Subtotal: €14.00"));
        assertFalse(receipt.contains("Discount"));
        assertTrue(receipt.contains("VAT (21%): €2.94"));
        assertTrue(receipt.contains("TOTAL: €16.94"));
    }

    @Test
    public void edgeCaseJustAboveThreshold() {
        Order order = new Order(Arrays.asList(
            new LineItem("item", 1, new BigDecimal("100.01"))
        ));
        String receipt = order.generateReceipt().toString();

        assertTrue(receipt.contains("Discount (10%): -€10.00"));
    }
}
