import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderProcessorTest {

    private final OrderProcessor processor = new OrderProcessor();

    @Test
    void appliesNoDiscountWhenSubtotalAtThreshold() {
        List<LineItem> items = List.of(new LineItem("Widget", 2, new BigDecimal("50.00")));

        Receipt receipt = processor.process(items);

        assertEquals(new BigDecimal("100.00"), receipt.subtotal());
        assertEquals(new BigDecimal("0.00"), receipt.discount());
        assertEquals(new BigDecimal("21.00"), receipt.vat());
        assertEquals(new BigDecimal("121.00"), receipt.total());
    }

    @Test
    void appliesDiscountWhenSubtotalExceedsThreshold() {
        List<LineItem> items = List.of(new LineItem("Widget", 3, new BigDecimal("40.00")));

        Receipt receipt = processor.process(items);

        assertEquals(new BigDecimal("120.00"), receipt.subtotal());
        assertEquals(new BigDecimal("12.00"), receipt.discount());
        assertEquals(new BigDecimal("22.68"), receipt.vat());
        assertEquals(new BigDecimal("130.68"), receipt.total());
    }

    @Test
    void rejectsEmptyOrder() {
        assertThrows(InvalidOrderException.class, () -> processor.process(List.of()));
    }

    @Test
    void rejectsBlankDescription() {
        List<LineItem> items = List.of(new LineItem(" ", 1, new BigDecimal("10.00")));

        assertThrows(InvalidOrderException.class, () -> processor.process(items));
    }

    @Test
    void rejectsNonPositiveQuantity() {
        List<LineItem> items = List.of(new LineItem("Widget", 0, new BigDecimal("10.00")));

        assertThrows(InvalidOrderException.class, () -> processor.process(items));
    }

    @Test
    void rejectsNegativeUnitPrice() {
        List<LineItem> items = List.of(new LineItem("Widget", 1, new BigDecimal("-1.00")));

        assertThrows(InvalidOrderException.class, () -> processor.process(items));
    }
}
