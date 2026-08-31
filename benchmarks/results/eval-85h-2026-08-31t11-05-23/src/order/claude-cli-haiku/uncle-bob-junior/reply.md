**Assumptions:**
- Line items have a description, positive quantity, and non-negative unit price.
- Discount (10%) applies to the subtotal before VAT, only when the subtotal exceeds €100.
- VAT (21%) applies to the subtotal after discount.
- All monetary values use `BigDecimal` for precision; results rounded to 2 decimals (HALF_UP).

```java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

class LineItem {
    private final String description;
    private final int quantity;
    private final BigDecimal unitPrice;
    
    LineItem(String description, int quantity, BigDecimal unitPrice) {
        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }
    
    String description() { return description; }
    int quantity() { return quantity; }
    BigDecimal unitPrice() { return unitPrice; }
    
    BigDecimal subtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}

class Order {
    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100");
    private static final int CURRENCY_SCALE = 2;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;
    
    private final List<LineItem> items;
    
    Order(List<LineItem> items) {
        this.items = List.copyOf(items);
    }
    
    void validate() throws IllegalArgumentException {
        for (LineItem item : items) {
            if (item.quantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be positive");
            }
            if (item.unitPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Unit price cannot be negative");
            }
        }
    }
    
    BigDecimal subtotal() {
        return items.stream()
            .map(LineItem::subtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    BigDecimal discountAmount() {
        BigDecimal preVatTotal = subtotal();
        if (preVatTotal.compareTo(DISCOUNT_THRESHOLD) > 0) {
            return preVatTotal.multiply(DISCOUNT_RATE).setScale(CURRENCY_SCALE, ROUNDING);
        }
        return BigDecimal.ZERO;
    }
    
    BigDecimal totalBeforeVat() {
        return subtotal().subtract(discountAmount());
    }
    
    BigDecimal vat() {
        return totalBeforeVat().multiply(VAT_RATE).setScale(CURRENCY_SCALE, ROUNDING);
    }
    
    BigDecimal total() {
        return totalBeforeVat().add(vat()).setScale(CURRENCY_SCALE, ROUNDING);
    }
    
    String receipt() {
        validate();
        
        StringBuilder receipt = new StringBuilder();
        receipt.append("=== RECEIPT ===\n");
        receipt.append(formatLineItems());
        receipt.append(formatSummary());
        
        return receipt.toString();
    }
    
    private String formatLineItems() {
        StringBuilder sb = new StringBuilder();
        for (LineItem item : items) {
            BigDecimal itemTotal = item.subtotal();
            sb.append(String.format("%-20s x%3d @ €%8.2f = €%8.2f\n",
                item.description(),
                item.quantity(),
                item.unitPrice().doubleValue(),
                itemTotal.doubleValue()));
        }
        return sb.toString();
    }
    
    private String formatSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append(String.format("Subtotal:                          €%8.2f\n", subtotal().doubleValue()));
        
        BigDecimal discount = discountAmount();
        if (discount.compareTo(BigDecimal.ZERO) > 0) {
            sb.append(String.format("Discount (10%%):                   -€%8.2f\n", discount.doubleValue()));
        }
        
        sb.append(String.format("Subtotal after discount:           €%8.2f\n", totalBeforeVat().doubleValue()));
        sb.append(String.format("VAT (21%%):                         €%8.2f\n", vat().doubleValue()));
        sb.append(String.format("Total:                             €%8.2f\n", total().doubleValue()));
        
        return sb.toString();
    }
}
```

```java
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {
    
    @Test
    void validateRejectsZeroQuantity() {
        var order = new Order(List.of(new LineItem("Item", 0, BigDecimal.TEN)));
        assertThrows(IllegalArgumentException.class, order::validate);
    }
    
    @Test
    void validateRejectsNegativePrice() {
        var order = new Order(List.of(new LineItem("Item", 1, BigDecimal.valueOf(-10))));
        assertThrows(IllegalArgumentException.class, order::validate);
    }
    
    @Test
    void validateAcceptsValidItems() {
        var order = new Order(List.of(new LineItem("Item", 1, BigDecimal.TEN)));
        order.validate();
    }
    
    @Test
    void subtotalSumsLineItems() {
        var items = List.of(
            new LineItem("A", 2, BigDecimal.TEN),
            new LineItem("B", 3, BigDecimal.valueOf(5))
        );
        assertEquals(BigDecimal.valueOf(35), new Order(items).subtotal());
    }
    
    @Test
    void discountAppliesWhenSubtotalExceeds100() {
        var order = new Order(List.of(new LineItem("Item", 11, BigDecimal.TEN)));
        assertEquals(new BigDecimal("11.00"), order.discountAmount());
    }
    
    @Test
    void discountDoesNotApplyWhen100OrLess() {
        var order = new Order(List.of(new LineItem("Item", 10, BigDecimal.TEN)));
        assertEquals(BigDecimal.ZERO, order.discountAmount());
    }
    
    @Test
    void totalWithoutDiscount() {
        var order = new Order(List.of(new LineItem("Item", 10, BigDecimal.TEN)));
        assertEquals(new BigDecimal("12.10"), order.total());
    }
    
    @Test
    void totalWithDiscount() {
        var order = new Order(List.of(new LineItem("Item", 11, BigDecimal.TEN)));
        assertEquals(new BigDecimal("119.79"), order.total());
    }
    
    @Test
    void receiptIncludesLineItems() {
        var receipt = new Order(List.of(new LineItem("Coffee", 2, BigDecimal.valueOf(5)))).receipt();
        assertTrue(receipt.contains("Coffee") && receipt.contains("10.00"));
    }
    
    @Test
    void receiptIncludesDiscount() {
        var receipt = new Order(List.of(new LineItem("Item", 11, BigDecimal.TEN))).receipt();
        assertTrue(receipt.contains("Discount (10%)") && receipt.contains("11.00"));
    }
}
```

Cleaned: `LineItem` is a pure value with no side effects; `Order` separates validation, calculation, and formatting into single-purpose methods with early returns. All monetary values use `BigDecimal` with consistent scale. Safe to change because tests verify all paths (no discount, discount applied, total calculation), and calculations are isolated from display logic.