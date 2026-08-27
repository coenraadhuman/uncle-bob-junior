import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderProcessorTest {

    private static LineItem item(String description, int quantity, String unitPrice) {
        return new LineItem(description, quantity, new BigDecimal(unitPrice));
    }

    @Test
    void computesVatWithoutDiscountBelowThreshold() {
        OrderTotals totals = OrderProcessor.computeTotals(List.of(item("Notebook", 2, "25.00")));

        assertEquals(new BigDecimal("50.00"), totals.subtotal());
        assertEquals(0, totals.discount().signum());
        assertEquals(new BigDecimal("10.50"), totals.vat());
        assertEquals(new BigDecimal("60.50"), totals.grandTotal());
    }

    @Test
    void noDiscountAtExactlyOneHundredEuros() {
        OrderTotals totals = OrderProcessor.computeTotals(List.of(item("Chair", 1, "100.00")));

        assertEquals(0, totals.discount().signum());
        assertEquals(new BigDecimal("121.00"), totals.grandTotal());
    }

    @Test
    void appliesDiscountAboveThresholdAndVatOnDiscountedAmount() {
        OrderTotals totals = OrderProcessor.computeTotals(List.of(item("Desk", 1, "200.00")));

        assertEquals(new BigDecimal("20.00"), totals.discount());
        assertEquals(new BigDecimal("37.80"), totals.vat());   // 21% of 180.00
        assertEquals(new BigDecimal("217.80"), totals.grandTotal());
    }

    @Test
    void receiptListsItemsAndTotals() {
        String receipt = OrderProcessor.processOrder(List.of(item("Desk", 1, "200.00")));

        assertTrue(receipt.contains("Desk"));
        assertTrue(receipt.contains("Discount (10%)"));
        assertTrue(receipt.contains("217.80"));
    }

    @Test
    void rejectsEmptyOrder() {
        assertThrows(IllegalArgumentException.class, () -> OrderProcessor.processOrder(List.of()));
    }

    @Test
    void rejectsInvalidLineItems() {
        assertThrows(IllegalArgumentException.class, () -> item(" ", 1, "10.00"));
        assertThrows(IllegalArgumentException.class, () -> item("Pen", 0, "10.00"));
        assertThrows(IllegalArgumentException.class, () -> item("Pen", 1, "-1.00"));
    }
}
