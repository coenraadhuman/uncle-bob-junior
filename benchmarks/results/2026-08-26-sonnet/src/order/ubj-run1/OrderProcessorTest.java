import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderProcessorTest {

    private final OrderProcessor processor = new OrderProcessor();

    @Test
    void appliesNoDiscountWhenSubtotalIsAtThreshold() {
        LineItem item = new LineItem("Widget", new BigDecimal("50"), 2); // subtotal = 100
        Receipt receipt = processor.processOrder(List.of(item));

        assertEquals(new BigDecimal("0.00"), receipt.discount());
        assertEquals(new BigDecimal("121.00"), receipt.total());
    }

    @Test
    void appliesDiscountWhenSubtotalExceedsThreshold() {
        LineItem item = new LineItem("Widget", new BigDecimal("60"), 2); // subtotal = 120
        Receipt receipt = processor.processOrder(List.of(item));

        assertEquals(new BigDecimal("12.00"), receipt.discount());
        assertEquals(new BigDecimal("130.68"), receipt.total()); // (120-12)*1.21
    }

    @Test
    void rejectsEmptyOrder() {
        assertThrows(InvalidOrderException.class, () -> processor.processOrder(List.of()));
    }

    @Test
    void rejectsNullOrder() {
        assertThrows(InvalidOrderException.class, () -> processor.processOrder(null));
    }

    @Test
    void rejectsLineItemWithBlankDescription() {
        assertThrows(InvalidOrderException.class,
                () -> new LineItem("  ", new BigDecimal("10"), 1));
    }

    @Test
    void rejectsLineItemWithNonPositivePrice() {
        assertThrows(InvalidOrderException.class,
                () -> new LineItem("Widget", BigDecimal.ZERO, 1));
    }

    @Test
    void rejectsLineItemWithNonPositiveQuantity() {
        assertThrows(InvalidOrderException.class,
                () -> new LineItem("Widget", new BigDecimal("10"), 0));
    }

    @Test
    void receiptStringContainsFormattedAmounts() {
        LineItem item = new LineItem("Widget", new BigDecimal("50"), 1); // subtotal = 50
        Receipt receipt = processor.processOrder(List.of(item));

        String receiptText = receipt.toReceiptString();

        assertEquals(true, receiptText.contains("€50.00"));
        assertEquals(true, receiptText.contains("€60.50")); // 50 * 1.21
    }
}
