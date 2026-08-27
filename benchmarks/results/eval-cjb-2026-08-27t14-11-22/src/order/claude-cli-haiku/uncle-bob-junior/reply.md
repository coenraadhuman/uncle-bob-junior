```java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class LineItem {
    private final String description;
    private final BigDecimal price;
    private final int quantity;
    
    public LineItem(String description, BigDecimal price, int quantity) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description cannot be null or blank");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        this.description = description;
        this.price = price;
        this.quantity = quantity;
    }
    
    public BigDecimal getSubtotal() {
        return price.multiply(new BigDecimal(quantity));
    }
    
    public String getDescription() {
        return description;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public int getQuantity() {
        return quantity;
    }
}
```

```java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final int DECIMAL_PLACES = 2;
    
    private final List<LineItem> lineItems;
    
    public Order(List<LineItem> lineItems) {
        if (lineItems == null || lineItems.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one line item");
        }
        this.lineItems = new ArrayList<>(lineItems);
    }
    
    public String generateReceipt() {
        BigDecimal subtotal = calculateSubtotal();
        BigDecimal discountAmount = calculateDiscount(subtotal);
        BigDecimal discountedSubtotal = subtotal.subtract(discountAmount);
        BigDecimal vatAmount = calculateVat(discountedSubtotal);
        BigDecimal total = discountedSubtotal.add(vatAmount);
        
        return formatReceipt(subtotal, discountAmount, discountedSubtotal, vatAmount, total);
    }
    
    private BigDecimal calculateSubtotal() {
        return lineItems.stream()
            .map(LineItem::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    private BigDecimal calculateDiscount(BigDecimal subtotal) {
        if (subtotal.compareTo(DISCOUNT_THRESHOLD) > 0) {
            return subtotal.multiply(DISCOUNT_RATE)
                .setScale(DECIMAL_PLACES, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }
    
    private BigDecimal calculateVat(BigDecimal amount) {
        return amount.multiply(VAT_RATE)
            .setScale(DECIMAL_PLACES, RoundingMode.HALF_UP);
    }
    
    private String formatReceipt(BigDecimal subtotal, BigDecimal discount,
                                 BigDecimal discountedSubtotal, BigDecimal vat,
                                 BigDecimal total) {
        StringBuilder receipt = new StringBuilder();
        receipt.append("=== ORDER RECEIPT ===\n");
        receipt.append(formatLineItems());
        receipt.append(String.format("\nSubtotal: €%.2f\n", subtotal));
        
        if (discount.compareTo(BigDecimal.ZERO) > 0) {
            receipt.append(String.format("Discount (10%%): -€%.2f\n", discount));
        }
        
        receipt.append(String.format("Subtotal after discount: €%.2f\n", discountedSubtotal));
        receipt.append(String.format("VAT (21%%): €%.2f\n", vat));
        receipt.append(String.format("\nTOTAL: €%.2f\n", total));
        receipt.append("====================");
        
        return receipt.toString();
    }
    
    private String formatLineItems() {
        StringBuilder items = new StringBuilder();
        for (LineItem item : lineItems) {
            items.append(String.format("  %s x%d @ €%.2f = €%.2f\n",
                item.getDescription(),
                item.getQuantity(),
                item.getPrice(),
                item.getSubtotal()));
        }
        return items.toString();
    }
}
```

```java
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class OrderTest {
    
    @Test
    public void validLineItemCreatesSuccessfully() {
        LineItem item = new LineItem("Widget", new BigDecimal("50.00"), 1);
        assertEquals("Widget", item.getDescription());
        assertEquals(new BigDecimal("50.00"), item.getPrice());
        assertEquals(1, item.getQuantity());
    }
    
    @Test
    public void lineItemSubtotalCalculation() {
        LineItem item = new LineItem("Widget", new BigDecimal("10.00"), 3);
        assertEquals(new BigDecimal("30.00"), item.getSubtotal());
    }
    
    @Test
    public void rejectsNullDescription() {
        assertThrows(IllegalArgumentException.class, () ->
            new LineItem(null, new BigDecimal("10.00"), 1));
    }
    
    @Test
    public void rejectsBlankDescription() {
        assertThrows(IllegalArgumentException.class, () ->
            new LineItem("   ", new BigDecimal("10.00"), 1));
    }
    
    @Test
    public void rejectsZeroOrNegativePrice() {
        assertThrows(IllegalArgumentException.class, () ->
            new LineItem("Widget", BigDecimal.ZERO, 1));
        assertThrows(IllegalArgumentException.class, () ->
            new LineItem("Widget", new BigDecimal("-10.00"), 1));
    }
    
    @Test
    public void rejectsZeroOrNegativeQuantity() {
        assertThrows(IllegalArgumentException.class, () ->
            new LineItem("Widget", new BigDecimal("10.00"), 0));
        assertThrows(IllegalArgumentException.class, () ->
            new LineItem("Widget", new BigDecimal("10.00"), -1));
    }
    
    @Test
    public void rejectsEmptyOrder() {
        assertThrows(IllegalArgumentException.class, () -> new Order(null));
        assertThrows(IllegalArgumentException.class, () -> new Order(List.of()));
    }
    
    @Test
    public void orderWithoutDiscountWhenSubtotalBelowThreshold() {
        Order order = new Order(List.of(
            new LineItem("Widget A", new BigDecimal("40.00"), 1),
            new LineItem("Widget B", new BigDecimal("50.00"), 1)
        ));
        
        String receipt = order.generateReceipt();
        assertTrue(receipt.contains("Subtotal: €90.00"));
        assertFalse(receipt.contains("Discount"));
        assertTrue(receipt.contains("VAT (21%): €18.90"));
        assertTrue(receipt.contains("TOTAL: €108.90"));
    }
    
    @Test
    public void orderWithDiscountWhenSubtotalExceeds100() {
        Order order = new Order(List.of(
            new LineItem("Widget A", new BigDecimal("60.00"), 1),
            new LineItem("Widget B", new BigDecimal("50.00"), 1)
        ));
        
        String receipt = order.generateReceipt();
        assertTrue(receipt.contains("Subtotal: €110.00"));
        assertTrue(receipt.contains("Discount (10%): -€11.00"));
        assertTrue(receipt.contains("Subtotal after discount: €99.00"));
        assertTrue(receipt.contains("VAT (21%): €20.79"));
        assertTrue(receipt.contains("TOTAL: €119.79"));
    }
    
    @Test
    public void receiptIncludesLineItemDetails() {
        Order order = new Order(List.of(
            new LineItem("Widget", new BigDecimal("25.00"), 2)
        ));
        
        String receipt = order.generateReceipt();
        assertTrue(receipt.contains("Widget x2 @ €25.00 = €50.00"));
    }
    
    @Test
    public void discountAppliedExactlyAt100EuroThreshold() {
        Order order = new Order(List.of(
            new LineItem("Item", new BigDecimal("100.00"), 1)
        ));
        
        String receipt = order.generateReceipt();
        assertFalse(receipt.contains("Discount"), "No discount at exactly €100.00");
        assertTrue(receipt.contains("TOTAL: €121.00"));
    }
    
    @Test
    public void discountAppliedJustAbove100EuroThreshold() {
        Order order = new Order(List.of(
            new LineItem("Item", new BigDecimal("100.01"), 1)
        ));
        
        String receipt = order.generateReceipt();
        assertTrue(receipt.contains("Discount (10%): -€10.00"));
        assertTrue(receipt.contains("TOTAL: €119.31"));
    }
}
```

**Assumptions:**
- Line items have non-empty description, positive price, and positive quantity
- Discount threshold is strictly greater than 100 EUR (not inclusive)
- VAT is applied after discount
- All monetary values use 2 decimal places with half-up rounding
- Receipt format is human-readable text

**Code structure:**
- `LineItem`: Immutable value object representing a product with validation
- `Order`: Processes line items, calculates subtotal, applies conditional discount, adds VAT, and generates receipt
- Helper methods each do one job: calculate subtotal, discount, VAT, and format output
- Comprehensive tests cover valid orders, validation failures, discount thresholds, and receipt format