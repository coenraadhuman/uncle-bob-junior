// ReceiptFormatterTest.java
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ReceiptFormatterTest {

    private final OrderCalculator calculator = new OrderCalculator();
    private final ReceiptFormatter formatter = new ReceiptFormatter();

    @Test
    void formatIncludesLineItemAndAllSummaryFigures() {
        LineItem item = new LineItem("participant_001", new BigDecimal("60.00"), 2);
        OrderSummary summary = calculator.summarize(List.of(item));

        String receipt = formatter.format(List.of(item), summary);

        assertTrue(receipt.contains("participant_001 x2"));
        assertTrue(receipt.contains("Subtotal: \u20AC120.00"));
        assertTrue(receipt.contains("Discount: -\u20AC12.00"));
        assertTrue(receipt.contains("VAT (21%): \u20AC22.68"));
        assertTrue(receipt.contains("Total: \u20AC130.68"));
    }
}
