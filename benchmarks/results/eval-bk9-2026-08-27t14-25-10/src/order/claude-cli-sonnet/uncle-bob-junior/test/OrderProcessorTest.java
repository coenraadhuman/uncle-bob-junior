// ---- OrderProcessorTest.java (JUnit 5) ----
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderProcessorTest {

    @Test
    void blankDescriptionIsRejected() {
        assertThrows(InvalidLineItemException.class,
                () -> new LineItem(" ", new BigDecimal("10.00"), 1));
    }

    @Test
    void zeroQuantityIsRejected() {
        assertThrows(InvalidLineItemException.class,
                () -> new LineItem("Ticket", new BigDecimal("10.00"), 0));
    }

    @Test
    void negativeUnitPriceIsRejected() {
        assertThrows(InvalidLineItemException.class,
                () -> new LineItem("Ticket", new BigDecimal("-1.00"), 1));
    }

    @Test
    void emptyOrderIsRejected() {
        assertThrows(InvalidLineItemException.class,
                () -> OrderCalculator.computeTotals(List.of()));
    }

    @Test
    void subtotalBelowThresholdHasNoDiscount() {
        List<LineItem> items = List.of(new LineItem("Ticket", new BigDecimal("20.00"), 2));

        OrderTotals totals = OrderCalculator.computeTotals(items);

        assertEquals(new BigDecimal("40.00"), totals.subtotal());
        assertEquals(new BigDecimal("0.00"), totals.discount());
        assertEquals(new BigDecimal("8.40"), totals.vat());
        assertEquals(new BigDecimal("48.40"), totals.total());
    }

    @Test
    void subtotalAtThresholdHasNoDiscount() {
        List<LineItem> items = List.of(new LineItem("Ticket", new BigDecimal("100.00"), 1));

        OrderTotals totals = OrderCalculator.computeTotals(items);

        assertEquals(new BigDecimal("0.00"), totals.discount());
        assertEquals(new BigDecimal("121.00"), totals.total());
    }

    @Test
    void subtotalAboveThresholdGetsDiscount() {
        List<LineItem> items = List.of(new LineItem("Ticket", new BigDecimal("150.00"), 1));

        OrderTotals totals = OrderCalculator.computeTotals(items);

        assertEquals(new BigDecimal("15.00"), totals.discount());
        assertEquals(new BigDecimal("28.35"), totals.vat());
        assertEquals(new BigDecimal("163.35"), totals.total());
    }

    @Test
    void receiptContainsExpectedTotalLine() {
        List<LineItem> items = List.of(new LineItem("Ticket", new BigDecimal("150.00"), 1));

        String receipt = OrderProcessor.process(items);

        assertTrue(receipt.contains("Total"));
        assertTrue(receipt.contains("163.35"));
    }
}
