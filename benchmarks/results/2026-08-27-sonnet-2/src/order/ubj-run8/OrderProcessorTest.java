// OrderProcessorTest.java
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class OrderProcessorTest {

    private final OrderProcessor processor = new OrderProcessor();

    @Test
    void rejectsOrderWithNoLineItems() {
        assertThrows(IllegalArgumentException.class, () -> new Order(List.of()));
    }

    @Test
    void appliesNoDiscountWhenSubtotalIsExactlyThreshold() {
        Order order = new Order(List.of(new LineItem("Widget", new BigDecimal("100.00"), 1)));
        OrderReceipt receipt = processor.process(order);

        assertEquals(new BigDecimal("0.00"), receipt.discount());
        assertEquals(new BigDecimal("21.00"), receipt.vat());
        assertEquals(new BigDecimal("121.00"), receipt.total());
    }

    @Test
    void appliesDiscountWhenSubtotalExceedsThreshold() {
        Order order = new Order(List.of(new LineItem("Widget", new BigDecimal("150.00"), 1)));
        OrderReceipt receipt = processor.process(order);

        assertEquals(new BigDecimal("15.00"), receipt.discount());
        assertEquals(new BigDecimal("28.35"), receipt.vat());
        assertEquals(new BigDecimal("163.35"), receipt.total());
    }

    @Test
    void computesSubtotalAcrossMultipleLineItems() {
        Order order = new Order(List.of(
                new LineItem("Widget", new BigDecimal("30.00"), 2),
                new LineItem("Gadget", new BigDecimal("10.00"), 1)));
        OrderReceipt receipt = processor.process(order);

        assertEquals(new BigDecimal("70.00"), receipt.subtotal());
        assertEquals(new BigDecimal("0.00"), receipt.discount());
    }
}
