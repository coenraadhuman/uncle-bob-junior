// OrderProcessorTest.java
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderProcessorTest {

    private final OrderProcessor processor = new OrderProcessor();

    @Test
    void producesReceiptForValidOrder() {
        List<RawLineItem> raw = List.of(new RawLineItem("Ticket", new BigDecimal("50.00"), 3));

        String receipt = processor.process(raw);

        assertTrue(receipt.contains("Subtotal: 150.00"));
        assertTrue(receipt.contains("Discount: -15.00"));
        assertTrue(receipt.contains("Total: 141.75"));
    }

    @Test
    void propagatesValidationFailure() {
        assertThrows(OrderValidationException.class, () -> processor.process(List.of()));
    }
}
