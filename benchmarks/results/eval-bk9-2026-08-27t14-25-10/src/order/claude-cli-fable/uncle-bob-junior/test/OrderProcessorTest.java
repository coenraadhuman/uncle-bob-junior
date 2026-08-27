import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderProcessorTest {

    private final OrderCalculator calculator = new OrderCalculator();
    private final OrderProcessor processor = new OrderProcessor();

    private static LineItem item(String price, int quantity) {
        return new LineItem("Widget", new BigDecimal(price), quantity);
    }

    @Test
    void computesVatWithoutDiscountBelowThreshold() {
        OrderTotals totals = calculator.totalsFor(List.of(item("10.00", 3)));
        assertEquals(new BigDecimal("30.00"), totals.subtotal());
        assertEquals(new BigDecimal("0.00"), totals.discount());
        assertEquals(new BigDecimal("6.30"), totals.vat());
        assertEquals(new BigDecimal("36.30"), totals.total());
    }

    @Test
    void appliesDiscountWhenSubtotalExceedsThreshold() {
        OrderTotals totals = calculator.totalsFor(List.of(item("60.00", 2)));
        assertEquals(new BigDecimal("120.00"), totals.subtotal());
        assertEquals(new BigDecimal("12.00"), totals.discount());
        assertEquals(new BigDecimal("22.68"), totals.vat());
        assertEquals(new BigDecimal("130.68"), totals.total());
    }

    @Test
    void noDiscountAtExactlyOneHundredEuros() {
        OrderTotals totals = calculator.totalsFor(List.of(item("100.00", 1)));
        assertEquals(new BigDecimal("0.00"), totals.discount());
        assertEquals(new BigDecimal("121.00"), totals.total());
    }

    @Test
    void rejectsEmptyOrNullOrder() {
        assertThrows(IllegalArgumentException.class, () -> calculator.totalsFor(List.of()));
        assertThrows(IllegalArgumentException.class, () -> calculator.totalsFor(null));
    }

    @Test
    void rejectsInvalidLineItems() {
        assertThrows(IllegalArgumentException.class, () -> new LineItem(" ", new BigDecimal("1.00"), 1));
        assertThrows(IllegalArgumentException.class, () -> new LineItem("Widget", new BigDecimal("-1.00"), 1));
        assertThrows(IllegalArgumentException.class, () -> new LineItem("Widget", null, 1));
        assertThrows(IllegalArgumentException.class, () -> new LineItem("Widget", new BigDecimal("1.00"), 0));
    }

    @Test
    void receiptListsItemsAndAllTotalRows() {
        String receipt = processor.process(List.of(item("60.00", 2)));
        assertTrue(receipt.startsWith("RECEIPT"));
        assertTrue(receipt.contains("Widget x 2 @ EUR 60.00 = EUR 120.00"));
        assertTrue(receipt.contains("Discount (10%):"));
        assertTrue(receipt.contains("VAT (21%):"));
        assertTrue(receipt.contains("Total:           EUR 130.68"));
    }

    @Test
    void receiptOmitsDiscountRowWhenNoDiscountApplies() {
        String receipt = processor.process(List.of(item("10.00", 1)));
        assertFalse(receipt.contains("Discount"));
        assertTrue(receipt.contains("Total:           EUR 12.10"));
    }
}
