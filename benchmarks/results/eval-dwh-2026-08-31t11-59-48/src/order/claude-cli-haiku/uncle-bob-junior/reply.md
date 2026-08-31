```java
import java.util.List;

public record LineItem(String name, int quantity, double unitPrice) {}

public class Order {
    private static final double VAT_RATE = 0.21;
    private static final double DISCOUNT_RATE = 0.10;
    private static final double DISCOUNT_THRESHOLD = 100.00;
    
    private final List<LineItem> lineItems;
    
    public Order(List<LineItem> lineItems) {
        validateLineItems(lineItems);
        this.lineItems = List.copyOf(lineItems);
    }
    
    private void validateLineItems(List<LineItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Line items cannot be null or empty");
        }
        items.forEach(this::validateLineItem);
    }
    
    private void validateLineItem(LineItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Line item cannot be null");
        }
        if (item.quantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (item.unitPrice() < 0) {
            throw new IllegalArgumentException("Unit price cannot be negative");
        }
    }
    
    public String generateReceipt() {
        double subtotal = calculateSubtotal();
        double discount = calculateDiscount(subtotal);
        double afterDiscount = subtotal - discount;
        double vat = calculateVat(afterDiscount);
        double total = afterDiscount + vat;
        
        return buildReceipt(subtotal, discount, afterDiscount, vat, total);
    }
    
    private double calculateSubtotal() {
        return lineItems.stream()
            .mapToDouble(item -> item.quantity() * item.unitPrice())
            .sum();
    }
    
    private double calculateDiscount(double subtotal) {
        return subtotal > DISCOUNT_THRESHOLD ? subtotal * DISCOUNT_RATE : 0.0;
    }
    
    private double calculateVat(double amountAfterDiscount) {
        return amountAfterDiscount * VAT_RATE;
    }
    
    private String buildReceipt(double subtotal, double discount, 
                                 double afterDiscount, double vat, double total) {
        StringBuilder receipt = new StringBuilder();
        receipt.append("=== ORDER RECEIPT ===\n");
        appendLineItems(receipt);
        receipt.append(String.format("\nSubtotal: €%.2f\n", subtotal));
        if (discount > 0.0) {
            receipt.append(String.format("Discount (10%%): -€%.2f\n", discount));
        }
        receipt.append(String.format("Amount after discount: €%.2f\n", afterDiscount));
        receipt.append(String.format("VAT (21%%): €%.2f\n", vat));
        receipt.append(String.format("TOTAL: €%.2f\n", total));
        return receipt.toString();
    }
    
    private void appendLineItems(StringBuilder receipt) {
        for (LineItem item : lineItems) {
            double lineTotal = item.quantity() * item.unitPrice();
            receipt.append(String.format("%s x%d @ €%.2f = €%.2f\n",
                item.name(), item.quantity(), item.unitPrice(), lineTotal));
        }
    }
}
```

```java
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class OrderTest {
    
    @Test
    void validateRejectsNullLineItems() {
        assertThrows(IllegalArgumentException.class, () -> new Order(null));
    }
    
    @Test
    void validateRejectsEmptyLineItems() {
        assertThrows(IllegalArgumentException.class, () -> new Order(new ArrayList<>()));
    }
    
    @Test
    void validateRejectsNullLineItem() {
        List<LineItem> items = new ArrayList<>();
        items.add(null);
        assertThrows(IllegalArgumentException.class, () -> new Order(items));
    }
    
    @Test
    void validateRejectsNegativeQuantity() {
        List<LineItem> items = List.of(new LineItem("Widget", -1, 10.0));
        assertThrows(IllegalArgumentException.class, () -> new Order(items));
    }
    
    @Test
    void validateRejectsZeroQuantity() {
        List<LineItem> items = List.of(new LineItem("Widget", 0, 10.0));
        assertThrows(IllegalArgumentException.class, () -> new Order(items));
    }
    
    @Test
    void validateRejectsNegativePrice() {
        List<LineItem> items = List.of(new LineItem("Widget", 1, -10.0));
        assertThrows(IllegalArgumentException.class, () -> new Order(items));
    }
    
    @Test
    void receiptWithoutDiscount() {
        List<LineItem> items = List.of(
            new LineItem("Apple", 10, 5.00),
            new LineItem("Orange", 5, 3.00)
        );
        Order order = new Order(items);
        String receipt = order.generateReceipt();
        
        assertTrue(receipt.contains("Subtotal: €65.00"));
        assertTrue(receipt.contains("VAT (21%): €13.65"));
        assertTrue(receipt.contains("TOTAL: €78.65"));
        assertFalse(receipt.contains("Discount (10%)"));
    }
    
    @Test
    void receiptWithDiscount() {
        List<LineItem> items = List.of(
            new LineItem("Widget", 20, 6.00)
        );
        Order order = new Order(items);
        String receipt = order.generateReceipt();
        
        assertTrue(receipt.contains("Subtotal: €120.00"));
        assertTrue(receipt.contains("Discount (10%): -€12.00"));
        assertTrue(receipt.contains("Amount after discount: €108.00"));
        assertTrue(receipt.contains("VAT (21%): €22.68"));
        assertTrue(receipt.contains("TOTAL: €130.68"));
    }
    
    @Test
    void noDiscountAtExactThreshold() {
        List<LineItem> items = List.of(
            new LineItem("Item", 100, 1.00)
        );
        Order order = new Order(items);
        String receipt = order.generateReceipt();
        
        assertTrue(receipt.contains("Subtotal: €100.00"));
        assertFalse(receipt.contains("Discount (10%)"));
    }
    
    @Test
    void discountAppliesAboveThreshold() {
        List<LineItem> items = List.of(
            new LineItem("Item", 101, 1.00)
        );
        Order order = new Order(items);
        String receipt = order.generateReceipt();
        
        assertTrue(receipt.contains("Subtotal: €101.00"));
        assertTrue(receipt.contains("Discount (10%): -€10.10"));
    }
}
```

**Assumptions made:**
- Line items have name, quantity (positive integer), and unit price (non-negative double).
- Validation rejects null items, empty list, null individual items, zero/negative quantity, and negative price.
- Discount threshold is strictly greater than 100 (100 does not trigger discount).
- VAT applies to the amount *after* discount.
- Amounts formatted to 2 decimal places (EUR currency).

**Cleaned:** Validation extracted to separate methods, all magic values named, LineItem is immutable, receipt generation separated into building and appending phases. Safe to extend (add tax codes, more line item fields) or modify thresholds and rates because constants are named and logic is isolated per responsibility.