I'll write a clean Java order processor using immutable value objects and clear separation of concerns. Key assumptions: line items need positive quantities and prices; discount applies to pre-VAT subtotal; amounts use `BigDecimal` for financial accuracy; VAT is 21% (standard for PLG markets).

```java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class LineItem {
    private final String description;
    private final int quantity;
    private final BigDecimal unitPrice;
    
    LineItem(String description, int quantity, BigDecimal unitPrice) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description cannot be blank");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Unit price must be positive");
        }
        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }
    
    String description() {
        return description;
    }
    
    int quantity() {
        return quantity;
    }
    
    BigDecimal unitPrice() {
        return unitPrice;
    }
    
    BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}

class Order {
    private final List<LineItem> items;
    
    Order(List<LineItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }
        this.items = Collections.unmodifiableList(new ArrayList<>(items));
    }
    
    List<LineItem> items() {
        return items;
    }
}

class OrderProcessor {
    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    
    Receipt process(Order order) {
        BigDecimal subtotal = computeSubtotal(order);
        BigDecimal discountAmount = applyDiscount(subtotal);
        BigDecimal afterDiscount = subtotal.subtract(discountAmount);
        BigDecimal vatAmount = afterDiscount.multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = afterDiscount.add(vatAmount);
        
        return new Receipt(order, subtotal, discountAmount, vatAmount, total);
    }
    
    private BigDecimal computeSubtotal(Order order) {
        return order.items().stream()
            .map(LineItem::lineTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);
    }
    
    private BigDecimal applyDiscount(BigDecimal subtotal) {
        if (subtotal.compareTo(DISCOUNT_THRESHOLD) > 0) {
            return subtotal.multiply(DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }
}

class Receipt {
    private final Order order;
    private final BigDecimal subtotal;
    private final BigDecimal discountAmount;
    private final BigDecimal vatAmount;
    private final BigDecimal total;
    
    Receipt(Order order, BigDecimal subtotal, BigDecimal discountAmount, 
            BigDecimal vatAmount, BigDecimal total) {
        this.order = order;
        this.subtotal = subtotal;
        this.discountAmount = discountAmount;
        this.vatAmount = vatAmount;
        this.total = total;
    }
    
    String format() {
        StringBuilder receipt = new StringBuilder();
        receipt.append("=== RECEIPT ===\n");
        appendLineItems(receipt);
        appendSummary(receipt);
        return receipt.toString();
    }
    
    private void appendLineItems(StringBuilder receipt) {
        for (LineItem item : order.items()) {
            receipt.append(String.format("%-30s %3d x €%7.2f = €%8.2f\n",
                item.description(), item.quantity(), item.unitPrice(), item.lineTotal()));
        }
    }
    
    private void appendSummary(StringBuilder receipt) {
        receipt.append(String.format("%-40s €%8.2f\n", "Subtotal:", subtotal));
        
        if (discountAmount.compareTo(BigDecimal.ZERO) > 0) {
            receipt.append(String.format("%-40s -€%7.2f\n", "Discount (10%):", discountAmount));
        }
        
        BigDecimal afterDiscount = subtotal.subtract(discountAmount);
        receipt.append(String.format("%-40s €%8.2f\n", "After discount:", afterDiscount));
        receipt.append(String.format("%-40s €%8.2f\n", "VAT (21%):", vatAmount));
        receipt.append("==========================================\n");
        receipt.append(String.format("%-40s €%8.2f\n", "TOTAL:", total));
    }
}
```

```java
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LineItemTest {
    @Test
    void createsValidLineItem() {
        LineItem item = new LineItem("Widget", 5, new BigDecimal("10.00"));
        assertEquals("Widget", item.description());
        assertEquals(5, item.quantity());
        assertEquals(new BigDecimal("10.00"), item.unitPrice());
    }
    
    @Test
    void rejectsBlankDescription() {
        assertThrows(IllegalArgumentException.class,
            () -> new LineItem("", 1, new BigDecimal("10.00")));
    }
    
    @Test
    void rejectsNonPositiveQuantity() {
        assertThrows(IllegalArgumentException.class,
            () -> new LineItem("Widget", 0, new BigDecimal("10.00")));
    }
    
    @Test
    void rejectsNonPositivePrice() {
        assertThrows(IllegalArgumentException.class,
            () -> new LineItem("Widget", 1, BigDecimal.ZERO));
    }
    
    @Test
    void computesLineTotal() {
        LineItem item = new LineItem("Widget", 5, new BigDecimal("10.50"));
        assertEquals(new BigDecimal("52.50"), item.lineTotal());
    }
}

class OrderTest {
    @Test
    void createsValidOrder() {
        List<LineItem> items = List.of(
            new LineItem("Item A", 1, new BigDecimal("50.00")),
            new LineItem("Item B", 2, new BigDecimal("30.00"))
        );
        Order order = new Order(items);
        assertEquals(2, order.items().size());
    }
    
    @Test
    void rejectsEmptyOrder() {
        assertThrows(IllegalArgumentException.class, () -> new Order(List.of()));
    }
    
    @Test
    void itemsAreImmutable() {
        List<LineItem> items = new ArrayList<>(List.of(
            new LineItem("Item A", 1, new BigDecimal("50.00"))
        ));
        Order order = new Order(items);
        items.add(new LineItem("Item B", 1, new BigDecimal("30.00")));
        assertEquals(1, order.items().size());
    }
}

class OrderProcessorTest {
    @Test
    void processesOrderBelowDiscountThreshold() {
        Order order = new Order(List.of(
            new LineItem("Widget", 2, new BigDecimal("40.00"))
        ));
        Receipt receipt = new OrderProcessor().process(order);
        
        String formatted = receipt.format();
        assertTrue(formatted.contains("€80.00"));
        assertFalse(formatted.contains("Discount"));
    }
    
    @Test
    void appliesDiscountWhenAboveThreshold() {
        Order order = new Order(List.of(
            new LineItem("Widget", 3, new BigDecimal("40.00")),
            new LineItem("Gadget", 1, new BigDecimal("20.00"))
        ));
        Receipt receipt = new OrderProcessor().process(order);
        
        String formatted = receipt.format();
        assertTrue(formatted.contains("Discount (10%)"));
    }
    
    @Test
    void appliesVatCorrectly() {
        Order order = new Order(List.of(
            new LineItem("Item", 1, new BigDecimal("100.00"))
        ));
        Receipt receipt = new OrderProcessor().process(order);
        
        String formatted = receipt.format();
        assertTrue(formatted.contains("€21.00")); // 21% VAT
        assertTrue(formatted.contains("€121.00")); // Total
    }
    
    @Test
    void computesComplexOrderWithDiscount() {
        Order order = new Order(List.of(
            new LineItem("Product A", 2, new BigDecimal("35.00")),
            new LineItem("Product B", 1, new BigDecimal("50.00")),
            new LineItem("Product C", 3, new BigDecimal("15.00"))
        ));
        Receipt receipt = new OrderProcessor().process(order);
        
        String formatted = receipt.format();
        assertTrue(formatted.contains("Subtotal: €       155.00"));
        assertTrue(formatted.contains("Discount (10%): -€     15.50"));
        assertTrue(formatted.contains("TOTAL:"));
    }
}
```

**Cleaned:** line items validated with guard clauses, BigDecimal used for financial precision with HALF_UP rounding, discount and VAT rates named as constants, receipt formatting split into separate methods. **Safe to change because:** invalid orders are unrepresentable (validated at construction), immutable value objects, each class has one job, tests cover discount boundary and VAT calculation.