// File: OrderProcessorTest.java
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class OrderProcessorTest {

    private final OrderProcessor processor = new OrderProcessor();

    @Test
    void rejectsNullLineItemList() {
        assertThrows(IllegalArgumentException.class, () -> processor.calculateTotals(null));
    }

    @Test
    void rejectsEmptyLineItemList() {
        assertThrows(IllegalArgumentException.class, () -> processor.calculateTotals(List.of()));
    }

    @Test
    void noDiscountWhenSubtotalAtThreshold() {
        List<LineItem> items = List.of(new LineItem("Widget", new BigDecimal("50"), 2));
        OrderTotals totals = processor.calculateTotals(items);

        assertEquals(new BigDecimal("100.00"), totals.subtotal());
        assertEquals(new BigDecimal("0.00"), totals.discount());
        assertEquals(new BigDecimal("21.00"), totals.vat());
        assertEquals(new BigDecimal("121.00"), totals.total());
    }

    @Test
    void appliesDiscountWhenSubtotalExceedsThreshold() {
        List<LineItem> items = List.of(new LineItem("Widget", new BigDecimal("60"), 2));
        OrderTotals totals = processor.calculateTotals(items);

        assertEquals(new BigDecimal("120.00"), totals.subtotal());
        assertEquals(new BigDecimal("12.00"), totals.discount());
        assertEquals(new BigDecimal("22.68"), totals.vat());
        assertEquals(new BigDecimal("130.68"), totals.total());
    }

    @Test
    void sumsMultipleLineItemsBeforeApplyingDiscountAndVat() {
        List<LineItem> items = List.of(
                new LineItem("Widget", new BigDecimal("30"), 2),
                new LineItem("Gadget", new BigDecimal("50"), 1));
        OrderTotals totals = processor.calculateTotals(items);

        assertEquals(new BigDecimal("110.00"), totals.subtotal());
        assertEquals(new BigDecimal("11.00"), totals.discount());
    }
}
