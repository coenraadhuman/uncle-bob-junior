import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderCalculatorTest {

    private static LineItem item(String description, String unitPrice, int quantity) {
        return new LineItem(description, new BigDecimal(unitPrice), quantity);
    }

    @Test
    void computesVatWithoutDiscountBelowThreshold() {
        OrderTotals totals = OrderCalculator.totalsFor(List.of(item("Ticket", "25.00", 3)));

        assertEquals(new BigDecimal("75.00"), totals.subtotal());
        assertEquals(new BigDecimal("0.00"), totals.discount());
        assertEquals(new BigDecimal("15.75"), totals.vat());
        assertEquals(new BigDecimal("90.75"), totals.grandTotal());
    }

    @Test
    void appliesDiscountWhenSubtotalExceedsThreshold() {
        OrderTotals totals = OrderCalculator.totalsFor(List.of(item("Bundle", "60.00", 2)));

        assertEquals(new BigDecimal("120.00"), totals.subtotal());
        assertEquals(new BigDecimal("12.00"), totals.discount());
        assertEquals(new BigDecimal("108.00"), totals.netAmount());
        assertEquals(new BigDecimal("22.68"), totals.vat());
        assertEquals(new BigDecimal("130.68"), totals.grandTotal());
    }

    @Test
    void exactlyOneHundredEurosGetsNoDiscount() {
        OrderTotals totals = OrderCalculator.totalsFor(List.of(item("Ticket", "100.00", 1)));

        assertEquals(new BigDecimal("0.00"), totals.discount());
        assertEquals(new BigDecimal("121.00"), totals.grandTotal());
    }

    @Test
    void rejectsEmptyOrder() {
        assertThrows(IllegalArgumentException.class, () -> OrderCalculator.totalsFor(List.of()));
    }

    @Test
    void rejectsInvalidLineItems() {
        assertThrows(IllegalArgumentException.class, () -> item(" ", "10.00", 1));
        assertThrows(IllegalArgumentException.class, () -> item("Ticket", "-1.00", 1));
        assertThrows(IllegalArgumentException.class, () -> item("Ticket", "10.00", 0));
    }

    @Test
    void receiptShowsDiscountRowOnlyWhenGranted() {
        String discounted = ReceiptFormatter.receiptFor(List.of(item("Bundle", "60.00", 2)));
        String plain = ReceiptFormatter.receiptFor(List.of(item("Ticket", "25.00", 3)));

        assertTrue(discounted.contains("Discount (10%)"));
        assertTrue(discounted.contains("130.68"));
        assertFalse(plain.contains("Discount"));
        assertTrue(plain.contains("90.75"));
    }
}
