import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class OrderProcessorTest {

    private final OrderProcessor processor = new OrderProcessor();

    @Test
    void computesVatWithoutDiscountBelowThreshold() {
        String receipt = processor.processOrder(
                List.of(new LineItem("Notebook", 2, new BigDecimal("10.00"))));

        assertTrue(receipt.contains("Subtotal (ex VAT)"));
        assertFalse(receipt.contains("Discount"));
        assertTrue(receipt.contains("VAT (21%)"));
        assertTrue(receipt.contains("24.20")); // 20.00 + 4.20 VAT
    }

    @Test
    void noDiscountAtExactlyOneHundredEuros() {
        String receipt = processor.processOrder(
                List.of(new LineItem("Chair", 1, new BigDecimal("100.00"))));

        assertFalse(receipt.contains("Discount"));
        assertTrue(receipt.contains("121.00")); // 100.00 + 21.00 VAT
    }

    @Test
    void appliesDiscountAboveThreshold() {
        String receipt = processor.processOrder(
                List.of(new LineItem("Desk", 1, new BigDecimal("200.00"))));

        assertTrue(receipt.contains("Discount (10%)"));
        assertTrue(receipt.contains("217.80")); // 180.00 + 37.80 VAT
    }

    @Test
    void rejectsEmptyOrder() {
        assertThrows(IllegalArgumentException.class,
                () -> processor.processOrder(List.of()));
    }

    @Test
    void rejectsNullOrder() {
        assertThrows(IllegalArgumentException.class,
                () -> processor.processOrder(null));
    }

    @Test
    void rejectsNonPositiveQuantity() {
        assertThrows(IllegalArgumentException.class, () -> processor.processOrder(
                List.of(new LineItem("Pen", 0, new BigDecimal("1.00")))));
    }

    @Test
    void rejectsNegativePrice() {
        assertThrows(IllegalArgumentException.class, () -> processor.processOrder(
                List.of(new LineItem("Pen", 1, new BigDecimal("-1.00")))));
    }

    @Test
    void rejectsBlankProductName() {
        assertThrows(IllegalArgumentException.class, () -> processor.processOrder(
                List.of(new LineItem("  ", 1, new BigDecimal("1.00")))));
    }

    @Test
    void roundsVatToCents() {
        String receipt = processor.processOrder(
                List.of(new LineItem("Sticker", 3, new BigDecimal("0.33"))));

        assertTrue(receipt.contains("1.20")); // 0.99 + 0.21 (0.2079 rounded) VAT
    }
}
