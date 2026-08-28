**Assumptions:**
- Line items require positive quantity and unit price; both are validated at construction.
- Discount threshold is strictly greater than 100 EUR (not ≥), applied before VAT.
- VAT and discount are calculated with 2 decimal places (HALF_UP rounding).
- Receipt displays items, subtotal, conditional discount, VAT, and total.

```java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

public class LineItem {
    private final String description;
    private final int quantity;
    private final BigDecimal unitPrice;
    
    public LineItem(String description, int quantity, BigDecimal unitPrice) {
        this.description = Objects.requireNonNull(description, "Description cannot be null");
        this.quantity = validateQuantity(quantity);
        this.unitPrice = validateUnitPrice(unitPrice);
    }
    
    private int validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        return quantity;
    }
    
    private BigDecimal validateUnitPrice(BigDecimal unitPrice) {
        Objects.requireNonNull(unitPrice, "Unit price cannot be null");
        if (unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Unit price must be positive");
        }
        return unitPrice;
    }
    
    public BigDecimal lineTotal() {
        return unitPrice.multiply(new BigDecimal(quantity));
    }
    
    public String getDescription() {
        return description;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public BigDecimal getUnitPrice() {
        return unitPrice;
    }
}

public class Order {
    private final List<LineItem> items;
    
    public Order(List<LineItem> items) {
        this.items = Objects.requireNonNull(items, "Items cannot be null");
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }
    }
    
    public List<LineItem> getItems() {
        return items;
    }
}

public class OrderReceipt {
    private final List<LineItem> items;
    private final BigDecimal subtotal;
    private final BigDecimal discount;
    private final BigDecimal vatAmount;
    private final BigDecimal total;
    
    public OrderReceipt(List<LineItem> items, BigDecimal subtotal, BigDecimal discount, 
                       BigDecimal vatAmount, BigDecimal total) {
        this.items = items;
        this.subtotal = subtotal;
        this.discount = discount;
        this.vatAmount = vatAmount;
        this.total = total;
    }
    
    public BigDecimal getSubtotal() {
        return subtotal;
    }
    
    public BigDecimal getDiscount() {
        return discount;
    }
    
    public BigDecimal getVatAmount() {
        return vatAmount;
    }
    
    public BigDecimal getTotal() {
        return total;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ORDER RECEIPT ===\n");
        
        for (LineItem item : items) {
            sb.append(String.format("%s (qty: %d x €%.2f) = €%.2f\n",
                item.getDescription(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.lineTotal()));
        }
        
        sb.append("\n");
        sb.append(String.format("Subtotal:        €%.2f\n", subtotal));
        
        if (discount.compareTo(BigDecimal.ZERO) > 0) {
            sb.append(String.format("Discount (10%%):  -€%.2f\n", discount));
        }
        
        sb.append(String.format("VAT (21%%):       €%.2f\n", vatAmount));
        sb.append(String.format("TOTAL:           €%.2f\n", total));
        
        return sb.toString();
    }
}

public class OrderProcessor {
    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100");
    private static final int SCALE = 2;
    
    public OrderReceipt process(Order order) {
        BigDecimal subtotal = calculateSubtotal(order);
        BigDecimal discount = calculateDiscount(subtotal);
        BigDecimal discountedAmount = subtotal.subtract(discount);
        BigDecimal vatAmount = calculateVat(discountedAmount);
        BigDecimal total = discountedAmount.add(vatAmount);
        
        return new OrderReceipt(order.getItems(), subtotal, discount, vatAmount, total);
    }
    
    private BigDecimal calculateSubtotal(Order order) {
        return order.getItems().stream()
            .map(LineItem::lineTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    private BigDecimal calculateDiscount(BigDecimal subtotal) {
        if (subtotal.compareTo(DISCOUNT_THRESHOLD) > 0) {
            return subtotal.multiply(DISCOUNT_RATE)
                .setScale(SCALE, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }
    
    private BigDecimal calculateVat(BigDecimal amount) {
        return amount.multiply(VAT_RATE)
            .setScale(SCALE, RoundingMode.HALF_UP);
    }
}
```

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class OrderProcessorTest {
    
    @Test
    public void lineItemValidatesPositiveQuantity() {
        assertThrows(IllegalArgumentException.class, () ->
            new LineItem("Widget", 0, new BigDecimal("10.00"))
        );
    }
    
    @Test
    public void lineItemValidatesPositivePrice() {
        assertThrows(IllegalArgumentException.class, () ->
            new LineItem("Widget", 1, new BigDecimal("0.00"))
        );
    }
    
    @Test
    public void lineItemCalculatesTotal() {
        LineItem item = new LineItem("Widget", 3, new BigDecimal("10.00"));
        assertEquals(new BigDecimal("30.00"), item.lineTotal());
    }
    
    @Test
    public void orderRequiresAtLeastOneItem() {
        assertThrows(IllegalArgumentException.class, () ->
            new Order(Arrays.asList())
        );
    }
    
    @Test
    public void processOrderWithNoDiscount() {
        List<LineItem> items = Arrays.asList(
            new LineItem("Widget", 5, new BigDecimal("10.00")),
            new LineItem("Gadget", 2, new BigDecimal("15.00"))
        );
        Order order = new Order(items);
        OrderReceipt receipt = new OrderProcessor().process(order);
        
        assertEquals(new BigDecimal("80.00"), receipt.getSubtotal());
        assertEquals(BigDecimal.ZERO, receipt.getDiscount());
        assertEquals(new BigDecimal("16.80"), receipt.getVatAmount());
        assertEquals(new BigDecimal("96.80"), receipt.getTotal());
    }
    
    @Test
    public void processOrderWithDiscount() {
        List<LineItem> items = Arrays.asList(
            new LineItem("Widget", 10, new BigDecimal("10.00")),
            new LineItem("Gadget", 5, new BigDecimal("5.00"))
        );
        Order order = new Order(items);
        OrderReceipt receipt = new OrderProcessor().process(order);
        
        assertEquals(new BigDecimal("125.00"), receipt.getSubtotal());
        assertEquals(new BigDecimal("12.50"), receipt.getDiscount());
        assertEquals(new BigDecimal("23.63"), receipt.getVatAmount());
        assertEquals(new BigDecimal("136.13"), receipt.getTotal());
    }
    
    @Test
    public void processOrderAtDiscountThreshold() {
        List<LineItem> items = Arrays.asList(
            new LineItem("Widget", 10, new BigDecimal("10.00"))
        );
        Order order = new Order(items);
        OrderReceipt receipt = new OrderProcessor().process(order);
        
        assertEquals(new BigDecimal("100.00"), receipt.getSubtotal());
        assertEquals(BigDecimal.ZERO, receipt.getDiscount());
        assertEquals(new BigDecimal("21.00"), receipt.getVatAmount());
        assertEquals(new BigDecimal("121.00"), receipt.getTotal());
    }
    
    @Test
    public void processOrderJustAboveThreshold() {
        List<LineItem> items = Arrays.asList(
            new LineItem("Widget", 101, new BigDecimal("1.00"))
        );
        Order order = new Order(items);
        OrderReceipt receipt = new OrderProcessor().process(order);
        
        assertEquals(new BigDecimal("101.00"), receipt.getSubtotal());
        assertEquals(new BigDecimal("10.10"), receipt.getDiscount());
        assertEquals(new BigDecimal("19.09"), receipt.getVatAmount());
        assertEquals(new BigDecimal("109.99"), receipt.getTotal());
    }
    
    @Test
    public void receiptStringFormattingWithDiscount() {
        List<LineItem> items = Arrays.asList(
            new LineItem("Widget", 6, new BigDecimal("20.00"))
        );
        Order order = new Order(items);
        String receiptString = new OrderProcessor().process(order).toString();
        
        assertTrue(receiptString.contains("Widget"));
        assertTrue(receiptString.contains("120.00"));
        assertTrue(receiptString.contains("Discount"));
    }
    
    @Test
    public void receiptStringFormattingWithoutDiscount() {
        List<LineItem> items = Arrays.asList(
            new LineItem("Widget", 2, new BigDecimal("50.00"))
        );
        Order order = new Order(items);
        String receiptString = new OrderProcessor().process(order).toString();
        
        assertTrue(receiptString.contains("100.00"));
        assertFalse(receiptString.contains("Discount"));
    }
}
```

**Cleaned:** LineItem and Order validate at construction (invalid states unrepresentable); OrderProcessor splits calculation concerns into focused private methods; OrderReceipt formats the receipt string and exposes results immutably. All behaviors tested across happy path and edge cases (threshold boundary, no discount, with discount).