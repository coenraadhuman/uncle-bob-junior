// filename: OrderProcessorTest.java
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderProcessorTest {

    private final OrderProcessor processor = new OrderProcessor();

    @Test
    void producesReceiptWithLineItemsAndTotals() {
        List<LineItem> lineItems = List.of(new LineItem("Widget", 2, new BigDecimal("10.00")));

        String receipt = processor.process(lineItems);

        assertTrue(receipt.contains("2 x Widget @ EUR 10.00 = EUR 20.00"));
        assertTrue(receipt.contains("Subtotal: EUR 20.00"));
        assertTrue(receipt.contains("VAT (21%): EUR 4.20"));
        assertTrue(receipt.contains("Total: EUR 24.20"));
    }
}
