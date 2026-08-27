// OrderReceiptTest.java
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderReceiptTest {

    @Test
    void rejectsNullOrder() {
        assertThrows(IllegalArgumentException.class, () -> OrderReceipt.process(null));
    }

    @Test
    void rejectsEmptyOrder() {
        assertThrows(IllegalArgumentException.class, () -> OrderReceipt.process(List.of()));
    }

    @Test
    void rejectsLineItemWithBlankDescription() {
        assertThrows(IllegalArgumentException.class,
                () -> new LineItem(" ", 1, new BigDecimal("10.00")));
    }

    @Test
    void rejectsLineItemWithZeroQuantity() {
        assertThrows(IllegalArgumentException.class,
                () -> new LineItem("Book", 0, new BigDecimal("10.00")));
    }

    @Test
    void rejectsLineItemWithNegativePrice() {
        assertThrows(IllegalArgumentException.class,
                () -> new LineItem("Book", 1, new BigDecimal("-1.00")));
    }

    @Test
    void appliesNoDiscountAtThreshold() {
        List<LineItem> items = List.of(new LineItem("Widget", 2, new BigDecimal("50.00")));
        String receipt = OrderReceipt.process(items);

        assertFalse(receipt.contains("Discount"));
        assertTrue(receipt.contains("121.00")); // 100.00 + 21% VAT
    }

    @Test
    void appliesDiscountAboveThreshold() {
        List<LineItem> items = List.of(new LineItem("Widget", 1, new BigDecimal("200.00")));
        String receipt = OrderReceipt.process(items);

        // subtotal 200.00, discount 20.00, discounted 180.00, vat 37.80, total 217.80
        assertTrue(receipt.contains("Discount"));
        assertTrue(receipt.contains("217.80"));
    }

    @Test
    void computesTotalsWithoutDiscountBelowThreshold() {
        List<LineItem> items = List.of(new LineItem("Widget", 1, new BigDecimal("50.00")));
        String receipt = OrderReceipt.process(items);

        // subtotal 50.00, vat 10.50, total 60.50
        assertFalse(receipt.contains("Discount"));
        assertTrue(receipt.contains("60.50"));
    }
}
