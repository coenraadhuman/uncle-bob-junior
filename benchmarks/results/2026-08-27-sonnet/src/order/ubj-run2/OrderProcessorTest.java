// OrderProcessorTest.java
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderProcessorTest {

    private final OrderProcessor processor = new OrderProcessor();

    @Test
    void rejectsEmptyOrder() {
        assertThrows(OrderValidationException.class, () -> processor.processOrder(List.of()));
    }

    @Test
    void rejectsNonPositiveQuantity() {
        List<OrderItem> items = List.of(new OrderItem("Widget", 0, new BigDecimal("10.00")));
        assertThrows(OrderValidationException.class, () -> processor.processOrder(items));
    }

    @Test
    void rejectsNegativeUnitPrice() {
        List<OrderItem> items = List.of(new OrderItem("Widget", 1, new BigDecimal("-1.00")));
        assertThrows(OrderValidationException.class, () -> processor.processOrder(items));
    }

    @Test
    void appliesNoDiscountAtThreshold() {
        List<OrderItem> items = List.of(new OrderItem("Widget", 1, new BigDecimal("100.00")));
        OrderPricing pricing = OrderPricing.of(items);
        assertEquals(new BigDecimal("0.00"), pricing.discount());
        assertEquals(new BigDecimal("121.00"), pricing.total());
    }

    @Test
    void appliesDiscountAboveThreshold() {
        List<OrderItem> items = List.of(new OrderItem("Widget", 1, new BigDecimal("200.00")));
        OrderPricing pricing = OrderPricing.of(items);
        assertEquals(new BigDecimal("20.00"), pricing.discount());
        assertEquals(new BigDecimal("217.80"), pricing.total());
    }

    @Test
    void receiptContainsTotal() {
        List<OrderItem> items = List.of(new OrderItem("Widget", 2, new BigDecimal("5.00")));
        String receipt = processor.processOrder(items);
        assertTrue(receipt.contains("Total"));
    }
}
