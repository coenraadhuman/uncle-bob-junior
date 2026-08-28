import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderProcessorTest {

    private final OrderProcessor processor = new OrderProcessor();

    private static LineItem item(String name, int quantity, String unitPrice) {
        return new LineItem(name, quantity, new BigDecimal(unitPrice));
    }

    @Test
    void pricesOrderBelowThresholdWithoutDiscount() {
        Receipt receipt = processor.process(new Order(List.of(item("Ticket", 2, "25.00"))));

        assertEquals(new BigDecimal("50.00"), receipt.subtotal());
        assertEquals(new BigDecimal("0.00"), receipt.discount());
        assertEquals(new BigDecimal("10.50"), receipt.vat());
        assertEquals(new BigDecimal("60.50"), receipt.total());
    }

    @Test
    void appliesTenPercentDiscountAboveHundredEuros() {
        Receipt receipt = processor.process(new Order(List.of(item("Bundle", 1, "200.00"))));

        assertEquals(new BigDecimal("200.00"), receipt.subtotal());
        assertEquals(new BigDecimal("20.00"), receipt.discount());
        assertEquals(new BigDecimal("37.80"), receipt.vat());
        assertEquals(new BigDecimal("217.80"), receipt.total());
    }

    @Test
    void exactlyHundredEurosGetsNoDiscount() {
        Receipt receipt = processor.process(new Order(List.of(item("Bundle", 4, "25.00"))));

        assertEquals(new BigDecimal("0.00"), receipt.discount());
        assertEquals(new BigDecimal("121.00"), receipt.total());
    }

    @Test
    void roundsVatHalfUpToCents() {
        // 33.33 * 0.21 = 6.9993, rounds to 7.00
        Receipt receipt = processor.process(new Order(List.of(item("Ticket", 1, "33.33"))));

        assertEquals(new BigDecimal("7.00"), receipt.vat());
        assertEquals(new BigDecimal("40.33"), receipt.total());
    }

    @Test
    void formatsReceiptWithAllAmounts() {
        Receipt receipt = processor.process(new Order(List.of(item("Bundle", 1, "200.00"))));

        String text = receipt.format();
        assertTrue(text.contains("Bundle x1 @ EUR 200.00 = EUR 200.00"));
        assertTrue(text.contains("Subtotal: EUR 200.00"));
        assertTrue(text.contains("Discount: EUR -20.00"));
        assertTrue(text.contains("VAT (21%): EUR 37.80"));
        assertTrue(text.contains("Total: EUR 217.80"));
    }

    @Test
    void receiptOmitsDiscountLineWhenNoDiscountApplies() {
        Receipt receipt = processor.process(new Order(List.of(item("Ticket", 1, "10.00"))));

        assertFalse(receipt.format().contains("Discount"));
    }

    @Test
    void rejectsInvalidLineItems() {
        assertThrows(IllegalArgumentException.class, () -> item(" ", 1, "10.00"));
        assertThrows(IllegalArgumentException.class, () -> item("Ticket", 0, "10.00"));
        assertThrows(IllegalArgumentException.class, () -> item("Ticket", -1, "10.00"));
        assertThrows(IllegalArgumentException.class, () -> item("Ticket", 1, "-0.01"));
    }

    @Test
    void rejectsEmptyOrder() {
        assertThrows(IllegalArgumentException.class, () -> new Order(List.of()));
        assertThrows(IllegalArgumentException.class, () -> new Order(null));
    }
}
