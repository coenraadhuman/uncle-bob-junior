I'll write a clean Java solution for order processing with validation, VAT calculation, discount logic, and receipt generation.

**Assumptions:**
- Prices use `BigDecimal` for financial accuracy
- Discount applies when subtotal **exceeds** €100 (not equals)
- VAT (21%) is calculated on the discounted subtotal
- Rounding uses HALF_UP to two decimal places

```java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class LineItem {
    private final int quantity;
    private final BigDecimal unitPrice;
    
    public LineItem(int quantity, BigDecimal unitPrice) {
        validateQuantity(quantity);
        validatePrice(unitPrice);
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }
    
    private void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
    }
    
    private void validatePrice(BigDecimal price) {
        if (price == null || price.signum() < 0) {
            throw new IllegalArgumentException("Price cannot be null or negative");
        }
    }
    
    public BigDecimal total() {
        return unitPrice.multiply(new BigDecimal(quantity));
    }
}

public class Order {
    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;
    
    private final List<LineItem> items;
    
    public Order() {
        this.items = new ArrayList<>();
    }
    
    public void addItem(LineItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Item cannot be null");
        }
        items.add(item);
    }
    
    public BigDecimal subtotal() {
        return items.stream()
            .map(LineItem::total)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public BigDecimal discountAmount() {
        BigDecimal sub = subtotal();
        if (sub.compareTo(DISCOUNT_THRESHOLD) > 0) {
            return sub.multiply(DISCOUNT_RATE).setScale(SCALE, ROUNDING);
        }
        return BigDecimal.ZERO;
    }
    
    public BigDecimal amountAfterDiscount() {
        return subtotal().subtract(discountAmount()).setScale(SCALE, ROUNDING);
    }
    
    public BigDecimal vat() {
        return amountAfterDiscount().multiply(VAT_RATE).setScale(SCALE, ROUNDING);
    }
    
    public BigDecimal total() {
        return amountAfterDiscount().add(vat()).setScale(SCALE, ROUNDING);
    }
    
    public String receipt() {
        StringBuilder sb = new StringBuilder();
        sb.append("RECEIPT\n");
        sb.append("=======\n");
        appendLineItems(sb);
        sb.append("-------\n");
        sb.append(String.format("Subtotal: €%.2f\n", subtotal()));
        appendDiscountIfApplied(sb);
        sb.append(String.format("VAT (21%%): €%.2f\n", vat()));
        sb.append("=======\n");
        sb.append(String.format("Total: €%.2f\n", total()));
        return sb.toString();
    }
    
    private void appendLineItems(StringBuilder sb) {
        for (LineItem item : items) {
            sb.append(String.format("Item: €%.2f\n", item.total()));
        }
    }
    
    private void appendDiscountIfApplied(StringBuilder sb) {
        if (discountAmount().signum() > 0) {
            sb.append(String.format("Discount (10%%): -€%.2f\n", discountAmount()));
        }
    }
}
```

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;

public class OrderTest {
    
    @Test
    void ordersUnder100ComputeVatWithoutDiscount() {
        Order order = new Order();
        order.addItem(new LineItem(1, new BigDecimal("50.00")));
        
        assertEquals(new BigDecimal("50.00"), order.subtotal());
        assertEquals(new BigDecimal("0.00"), order.discountAmount());
        assertEquals(new BigDecimal("10.50"), order.vat());
        assertEquals(new BigDecimal("60.50"), order.total());
    }
    
    @Test
    void ordersExceeding100ApplyDiscount() {
        Order order = new Order();
        order.addItem(new LineItem(2, new BigDecimal("60.00")));
        
        assertEquals(new BigDecimal("120.00"), order.subtotal());
        assertEquals(new BigDecimal("12.00"), order.discountAmount());
        assertEquals(new BigDecimal("108.00"), order.amountAfterDiscount());
        assertEquals(new BigDecimal("22.68"), order.vat());
        assertEquals(new BigDecimal("130.68"), order.total());
    }
    
    @Test
    void exactly100DoesNotQualifyForDiscount() {
        Order order = new Order();
        order.addItem(new LineItem(1, new BigDecimal("100.00")));
        
        assertEquals(new BigDecimal("0.00"), order.discountAmount());
        assertEquals(new BigDecimal("21.00"), order.vat());
        assertEquals(new BigDecimal("121.00"), order.total());
    }
    
    @Test
    void multipleItemsAreIncludedInCalculations() {
        Order order = new Order();
        order.addItem(new LineItem(2, new BigDecimal("40.00")));
        order.addItem(new LineItem(1, new BigDecimal("35.00")));
        
        assertEquals(new BigDecimal("115.00"), order.subtotal());
        assertEquals(new BigDecimal("11.50"), order.discountAmount());
        assertEquals(new BigDecimal("21.74"), order.vat());
        assertEquals(new BigDecimal("125.24"), order.total());
    }
    
    @Test
    void invalidQuantityThrows() {
        assertThrows(IllegalArgumentException.class, 
            () -> new LineItem(0, new BigDecimal("50.00")));
        assertThrows(IllegalArgumentException.class, 
            () -> new LineItem(-5, new BigDecimal("50.00")));
    }
    
    @Test
    void invalidPriceThrows() {
        assertThrows(IllegalArgumentException.class, 
            () -> new LineItem(1, null));
        assertThrows(IllegalArgumentException.class, 
            () -> new LineItem(1, new BigDecimal("-50.00")));
    }
    
    @Test
    void nullItemThrows() {
        Order order = new Order();
        assertThrows(IllegalArgumentException.class, 
            () -> order.addItem(null));
    }
    
    @Test
    void receiptIncludesAllCalculations() {
        Order order = new Order();
        order.addItem(new LineItem(1, new BigDecimal("50.00")));
        
        String receipt = order.receipt();
        assertTrue(receipt.contains("RECEIPT"));
        assertTrue(receipt.contains("Subtotal: €50.00"));
        assertTrue(receipt.contains("VAT (21%): €10.50"));
        assertTrue(receipt.contains("Total: €60.50"));
    }
    
    @Test
    void receiptShowsDiscountWhenApplied() {
        Order order = new Order();
        order.addItem(new LineItem(2, new BigDecimal("60.00")));
        
        String receipt = order.receipt();
        assertTrue(receipt.contains("Discount (10%): -€12.00"));
    }
}
```

**Cleaned:** Each class has one responsibility (item validation and totaling; order aggregation and calculations). Named constants eliminate magic numbers. Guard clauses validate at boundaries. Immutable BigDecimal and final fields prevent accidental state changes. Tests cover the happy path (under/over threshold), edges (exactly €100, null/negative inputs), and receipt output.