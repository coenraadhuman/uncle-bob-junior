import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderProcessorTest {

    private final OrderProcessor processor = new OrderProcessor();

    private static OrderProcessor.LineItem item(String name, int quantity, String unitPrice) {
        return new OrderProcessor.LineItem(name, quantity, new BigDecimal(unitPrice));
    }

    @Test
    void computesVatWithoutDiscountAtOrBelowThreshold() {
        String receipt = processor.process(List.of(item("Notebook", 2, "50.00")));

        assertFalse(receipt.contains("Discount"));
        assertTrue(receipt.contains("VAT (21%): EUR 21.00"));
        assertTrue(receipt.contains("Total: EUR 121.00"));
    }

    @Test
    void appliesDiscountAboveThresholdBeforeVat() {
        String receipt = processor.process(List.of(item("Desk", 1, "200.00")));

        assertTrue(receipt.contains("Discount (10%): -EUR 20.00"));
        assertTrue(receipt.contains("VAT (21%): EUR 37.80"));
        assertTrue(receipt.contains("Total: EUR 217.80"));
    }

    @Test
    void listsEachLineItemOnReceipt() {
        String receipt = processor.process(List.of(item("Pen", 3, "1.50")));

        assertTrue(receipt.contains("3 x Pen @ EUR 1.50 = EUR 4.50"));
    }

    @Test
    void rejectsEmptyOrder() {
        assertThrows(IllegalArgumentException.class, () -> processor.process(List.of()));
    }

    @Test
    void rejectsNullOrder() {
        assertThrows(IllegalArgumentException.class, () -> processor.process(null));
    }

    @Test
    void rejectsBlankName() {
        assertThrows(IllegalArgumentException.class,
                () -> processor.process(List.of(item("  ", 1, "10.00"))));
    }

    @Test
    void rejectsZeroQuantity() {
        assertThrows(IllegalArgumentException.class,
                () -> processor.process(List.of(item("Pen", 0, "10.00"))));
    }

    @Test
    void rejectsNegativePrice() {
        assertThrows(IllegalArgumentException.class,
                () -> processor.process(List.of(item("Pen", 1, "-1.00"))));
    }

    @Test
    void boundaryExactlyOneHundredGetsNoDiscount() {
        String receipt = processor.process(List.of(item("Chair", 1, "100.00")));

        assertFalse(receipt.contains("Discount"));
        assertTrue(receipt.contains("Total: EUR 121.00"));
    }
}
