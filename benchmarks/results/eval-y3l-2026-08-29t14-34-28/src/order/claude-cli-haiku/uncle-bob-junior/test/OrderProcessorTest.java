import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;

public class OrderProcessorTest {

    @Test
    void validatesLineItemDescription() {
        assertThrows(IllegalArgumentException.class,
            () -> new OrderProcessor.LineItem("", new BigDecimal("10.00"), 1));
        assertThrows(IllegalArgumentException.class,
            () -> new OrderProcessor.LineItem(null, new BigDecimal("10.00"), 1));
    }

    @Test
    void validatesLineItemPrice() {
        assertThrows(IllegalArgumentException.class,
            () -> new OrderProcessor.LineItem("Item", new BigDecimal("-5.00"), 1));
        assertThrows(IllegalArgumentException.class,
            () -> new OrderProcessor.LineItem("Item", null, 1));
    }

    @Test
    void validatesLineItemQuantity() {
        assertThrows(IllegalArgumentException.class,
            () -> new OrderProcessor.LineItem("Item", new BigDecimal("10.00"), 0));
        assertThrows(IllegalArgumentException.class,
            () -> new OrderProcessor.LineItem("Item", new BigDecimal("10.00"), -1));
    }

    @Test
    void calculatesLineTotalCorrectly() {
        var item = new OrderProcessor.LineItem("Widget", new BigDecimal("25.00"), 3);
        assertEquals(new BigDecimal("75.00"), item.lineTotal());
    }

    @Test
    void requiresAtLeastOneItem() {
        assertThrows(IllegalArgumentException.class,
            () -> OrderProcessor.process(Collections.emptyList()));
        assertThrows(IllegalArgumentException.class,
            () -> OrderProcessor.process(null));
    }

    @Test
    void computesVatWithoutDiscountWhenBelowThreshold() {
        var items = Arrays.asList(
            new OrderProcessor.LineItem("Item A", new BigDecimal("50.00"), 1)
        );
        var result = OrderProcessor.process(items);

        assertEquals(new BigDecimal("50.00"), result.getSubtotal());
        assertEquals(BigDecimal.ZERO, result.getDiscount());
        assertEquals(new BigDecimal("10.50"), result.getVat());
        assertEquals(new BigDecimal("60.50"), result.getTotal());
    }

    @Test
    void appliesDiscountAboveThreshold() {
        var items = Arrays.asList(
            new OrderProcessor.LineItem("Item A", new BigDecimal("60.00"), 1),
            new OrderProcessor.LineItem("Item B", new BigDecimal("50.00"), 1)
        );
        var result = OrderProcessor.process(items);

        assertEquals(new BigDecimal("110.00"), result.getSubtotal());
        assertEquals(new BigDecimal("11.00"), result.getDiscount());
        assertEquals(new BigDecimal("20.79"), result.getVat());
        assertEquals(new BigDecimal("119.79"), result.getTotal());
    }

    @Test
    void noDiscountAtThresholdBoundary() {
        var items = Arrays.asList(
            new OrderProcessor.LineItem("Item", new BigDecimal("100.00"), 1)
        );
        var result = OrderProcessor.process(items);

        assertEquals(new BigDecimal("100.00"), result.getSubtotal());
        assertEquals(BigDecimal.ZERO, result.getDiscount());
        assertEquals(new BigDecimal("21.00"), result.getVat());
        assertEquals(new BigDecimal("121.00"), result.getTotal());
    }

    @Test
    void producesReceiptWithLineItems() {
        var items = Arrays.asList(
            new OrderProcessor.LineItem("Coffee", new BigDecimal("3.50"), 2),
            new OrderProcessor.LineItem("Sandwich", new BigDecimal("8.00"), 1)
        );
        var result = OrderProcessor.process(items);
        String receipt = result.getReceipt();

        assertTrue(receipt.contains("Coffee x2 @ €3.50 = €7.00"));
        assertTrue(receipt.contains("Sandwich x1 @ €8.00 = €8.00"));
        assertTrue(receipt.contains("Subtotal: €15.00"));
        assertTrue(receipt.contains("VAT (21%)"));
        assertTrue(receipt.contains("TOTAL:"));
    }

    @Test
    void receiptIncludesDiscountWhenApplied() {
        var items = Arrays.asList(
            new OrderProcessor.LineItem("Premium Item", new BigDecimal("105.00"), 1)
        );
        var result = OrderProcessor.process(items);
        String receipt = result.getReceipt();

        assertTrue(receipt.contains("Discount (10%): -€10.50"));
    }
}
