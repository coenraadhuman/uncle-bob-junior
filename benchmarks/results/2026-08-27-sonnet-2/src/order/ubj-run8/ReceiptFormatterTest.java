// ReceiptFormatterTest.java
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ReceiptFormatterTest {

    private final ReceiptFormatter formatter = new ReceiptFormatter();

    @Test
    void includesEachLineItemAndTotals() {
        List<LineItem> items = List.of(new LineItem("Widget", new BigDecimal("50.00"), 1));
        OrderReceipt receipt = new OrderReceipt(items,
                new BigDecimal("50.00"), new BigDecimal("0.00"),
                new BigDecimal("10.50"), new BigDecimal("60.50"));

        String result = formatter.format(receipt);

        assertTrue(result.contains("Widget x1: EUR 50.00"));
        assertTrue(result.contains("Subtotal: EUR 50.00"));
        assertTrue(result.contains("Discount: -EUR 0.00"));
        assertTrue(result.contains("VAT (21%): EUR 10.50"));
        assertTrue(result.contains("Total: EUR 60.50"));
    }
}
