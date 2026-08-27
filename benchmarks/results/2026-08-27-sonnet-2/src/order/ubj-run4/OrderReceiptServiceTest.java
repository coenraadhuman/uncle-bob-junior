// File: OrderReceiptServiceTest.java
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class OrderReceiptServiceTest {

    private final OrderReceiptService service =
            new OrderReceiptService(new OrderProcessor(), new ReceiptFormatter());

    @Test
    void receiptContainsLineItemsAndTotals() {
        List<LineItem> items = List.of(new LineItem("Widget", new BigDecimal("60"), 2));
        String receipt = service.generateReceipt(items);

        assertTrue(receipt.contains("Widget"));
        assertTrue(receipt.contains("Subtotal"));
        assertTrue(receipt.contains("Discount"));
        assertTrue(receipt.contains("VAT (21%)"));
        assertTrue(receipt.contains("Total"));
    }
}
