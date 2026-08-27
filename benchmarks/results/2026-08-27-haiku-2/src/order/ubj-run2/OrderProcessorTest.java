import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class OrderProcessorTest {
    @Test
    void processesOrderWithoutDiscount() {
        List<LineItem> items = List.of(
            new LineItem("Milk", 2, new BigDecimal("3.50")),
            new LineItem("Bread", 1, new BigDecimal("2.00"))
        );
        
        String receipt = OrderProcessor.processOrder(items);
        
        assertTrue(receipt.contains("Milk x2 @ €3.50 = €7.00"));
        assertTrue(receipt.contains("Bread x1 @ €2.00 = €2.00"));
        assertTrue(receipt.contains("Subtotal:    €9.00"));
        assertTrue(receipt.contains("VAT (21%):    €1.89"));
        assertTrue(receipt.contains("TOTAL:        €10.89"));
        assertFalse(receipt.contains("Discount"));
    }

    @Test
    void appliesDiscountWhenSubtotalExceeds100() {
        List<LineItem> items = List.of(
            new LineItem("Laptop", 1, new BigDecimal("150.00"))
        );
        
        String receipt = OrderProcessor.processOrder(items);
        
        assertTrue(receipt.contains("Subtotal:    €150.00"));
        assertTrue(receipt.contains("Discount:   -€15.00"));
        assertTrue(receipt.contains("After disc:   €135.00"));
        assertTrue(receipt.contains("VAT (21%):    €28.35"));
        assertTrue(receipt.contains("TOTAL:        €163.35"));
    }

    @Test
    void throwsOnInvalidQuantity() {
        assertThrows(IllegalArgumentException.class,
            () -> new LineItem("Item", 0, new BigDecimal("10.00")));
    }

    @Test
    void throwsOnInvalidPrice() {
        assertThrows(IllegalArgumentException.class,
            () -> new LineItem("Item", 1, BigDecimal.ZERO));
    }

    @Test
    void throwsOnEmptyOrder() {
        assertThrows(IllegalArgumentException.class,
            () -> OrderProcessor.processOrder(List.of()));
    }
}
