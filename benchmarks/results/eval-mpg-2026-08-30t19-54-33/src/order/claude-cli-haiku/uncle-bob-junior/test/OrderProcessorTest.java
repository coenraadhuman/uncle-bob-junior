import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class OrderProcessorTest {

    @Test
    void rejectsNullDescription() {
        assertThrows(IllegalArgumentException.class,
            () -> new LineItem(null, 1, new BigDecimal("10.00")));
    }

    @Test
    void rejectsEmptyDescription() {
        assertThrows(IllegalArgumentException.class,
            () -> new LineItem("   ", 1, new BigDecimal("10.00")));
    }

    @Test
    void rejectsZeroOrNegativeQuantity() {
        assertThrows(IllegalArgumentException.class,
            () -> new LineItem("Item", 0, new BigDecimal("10.00")));
        assertThrows(IllegalArgumentException.class,
            () -> new LineItem("Item", -1, new BigDecimal("10.00")));
    }

    @Test
    void rejectsNegativeOrNullPrice() {
        assertThrows(IllegalArgumentException.class,
            () -> new LineItem("Item", 1, new BigDecimal("-10.00")));
        assertThrows(IllegalArgumentException.class,
            () -> new LineItem("Item", 1, null));
    }

    @Test
    void rejectsEmptyOrder() {
        assertThrows(IllegalArgumentException.class, () -> new Order(Collections.emptyList()));
        assertThrows(IllegalArgumentException.class, () -> new Order(null));
    }

    @Test
    void computesTotalWithVatAndNoDiscount() {
        LineItem item1 = new LineItem("Apples", 2, new BigDecimal("10.00"));
        LineItem item2 = new LineItem("Oranges", 1, new BigDecimal("15.00"));
        Order order = new Order(Arrays.asList(item1, item2));

        Receipt receipt = order.process();

        assertEquals(new BigDecimal("35.00"), receipt.getPreTaxTotal());
        assertEquals(new BigDecimal("0.00"), receipt.getDiscountAmount());
        assertEquals(new BigDecimal("7.35"), receipt.getVatAmount());
        assertEquals(new BigDecimal("42.35"), receipt.getFinalTotal());
    }

    @Test
    void appliesDiscountWhenPreTaxTotalExceeds100() {
        LineItem item = new LineItem("Premium Package", 1, new BigDecimal("120.00"));
        Order order = new Order(Collections.singletonList(item));

        Receipt receipt = order.process();

        assertEquals(new BigDecimal("120.00"), receipt.getPreTaxTotal());
        assertEquals(new BigDecimal("12.00"), receipt.getDiscountAmount());
        assertEquals(new BigDecimal("22.68"), receipt.getVatAmount());
        assertEquals(new BigDecimal("130.68"), receipt.getFinalTotal());
    }

    @Test
    void producesReceiptStringWithoutDiscount() {
        LineItem item = new LineItem("Coffee", 2, new BigDecimal("3.50"));
        Order order = new Order(Collections.singletonList(item));

        String receipt = order.process().toString();

        assertTrue(receipt.contains("Coffee: €7.00"));
        assertTrue(receipt.contains("Subtotal: €7.00"));
        assertTrue(receipt.contains("VAT (21%): €1.47"));
        assertTrue(receipt.contains("Total: €8.47"));
        assertFalse(receipt.contains("Discount"));
    }

    @Test
    void producesReceiptStringWithDiscount() {
        LineItem item = new LineItem("Item", 1, new BigDecimal("100.01"));
        Order order = new Order(Collections.singletonList(item));

        String receipt = order.process().toString();

        assertTrue(receipt.contains("Subtotal: €100.01"));
        assertTrue(receipt.contains("Discount (10%): -€10.00"));
        assertTrue(receipt.contains("Total: €96.31"));
    }
}
