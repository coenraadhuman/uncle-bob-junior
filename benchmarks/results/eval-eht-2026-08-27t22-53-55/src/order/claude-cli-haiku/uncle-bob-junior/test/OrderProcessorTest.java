import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderProcessorTest {

    @Test
    void lineItemCalculatesLineTotalCorrectly() {
        OrderProcessor.LineItem item = new OrderProcessor.LineItem("Widget", new BigDecimal("10.50"), 4);
        assertEquals(new BigDecimal("42.00"), item.getLineTotal());
    }

    @Test
    void lineItemRejectsNullDescription() {
        assertThrows(IllegalArgumentException.class, () ->
            new OrderProcessor.LineItem(null, new BigDecimal("10.00"), 1)
        );
    }

    @Test
    void lineItemRejectsBlankDescription() {
        assertThrows(IllegalArgumentException.class, () ->
            new OrderProcessor.LineItem("   ", new BigDecimal("10.00"), 1)
        );
    }

    @Test
    void lineItemRejectsNegativePrice() {
        assertThrows(IllegalArgumentException.class, () ->
            new OrderProcessor.LineItem("Widget", new BigDecimal("-5.00"), 1)
        );
    }

    @Test
    void lineItemRejectsZeroQuantity() {
        assertThrows(IllegalArgumentException.class, () ->
            new OrderProcessor.LineItem("Widget", new BigDecimal("10.00"), 0)
        );
    }

    @Test
    void orderRejectsEmptyItemList() {
        assertThrows(IllegalArgumentException.class, () ->
            new OrderProcessor.Order(Arrays.asList())
        );
    }

    @Test
    void orderCalculatesTotalWithVatWithoutDiscount() {
        List<OrderProcessor.LineItem> items = Arrays.asList(
            new OrderProcessor.LineItem("Item A", new BigDecimal("40.00"), 1),
            new OrderProcessor.LineItem("Item B", new BigDecimal("50.00"), 1)
        );
        OrderProcessor.Order order = new OrderProcessor.Order(items);

        // Subtotal: 90.00 (below threshold, no discount)
        // VAT (21%): 18.90
        // Total: 108.90
        assertEquals(new BigDecimal("108.90"), order.calculateTotal());
    }

    @Test
    void orderCalculatesTotalWithVatAndDiscount() {
        List<OrderProcessor.LineItem> items = Arrays.asList(
            new OrderProcessor.LineItem("Item A", new BigDecimal("60.00"), 1),
            new OrderProcessor.LineItem("Item B", new BigDecimal("50.00"), 1)
        );
        OrderProcessor.Order order = new OrderProcessor.Order(items);

        // Subtotal: 110.00
        // Discount (10%): 11.00
        // Pre-VAT: 99.00
        // VAT (21%): 20.79
        // Total: 119.79
        assertEquals(new BigDecimal("119.79"), order.calculateTotal());
    }

    @Test
    void orderGeneratesReceiptWithAllDetails() {
        List<OrderProcessor.LineItem> items = Arrays.asList(
            new OrderProcessor.LineItem("Widget", new BigDecimal("10.00"), 2),
            new OrderProcessor.LineItem("Gadget", new BigDecimal("50.00"), 3)
        );
        OrderProcessor.Order order = new OrderProcessor.Order(items);
        String receipt = order.generateReceipt();

        assertTrue(receipt.contains("Widget"));
        assertTrue(receipt.contains("Gadget"));
        assertTrue(receipt.contains("€"));
        assertTrue(receipt.contains("Subtotal"));
        assertTrue(receipt.contains("TOTAL"));
    }

    @Test
    void orderReceiptShowsDiscountOnlyWhenApplied() {
        List<OrderProcessor.LineItem> items = Arrays.asList(
            new OrderProcessor.LineItem("Cheap Item", new BigDecimal("50.00"), 1)
        );
        OrderProcessor.Order order = new OrderProcessor.Order(items);
        String receipt = order.generateReceipt();

        assertFalse(receipt.contains("Discount (10%)"));
    }
}
