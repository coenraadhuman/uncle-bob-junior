I'll write clean, tested Java code for order processing with the stated requirements.

**Assumptions:**
- Line items have description, quantity, and unit price
- All amounts in euros
- Validation fails on non-positive quantities or negative prices
- Discount applies to pre-VAT subtotal; VAT then applies to the discounted amount
- Receipt shows itemized details with currency formatting

```java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

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
    private static final int SCALE = 2;

    private final List<LineItem> items;

    Order(List<LineItem> items) {
        validate(items);
        this.items = items;
    }

    private void validate(List<LineItem> items) {
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }
        for (LineItem item : items) {
            if (item.quantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be positive");
            }
            if (item.unitPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Price cannot be negative");
            }
        }
    }

    private BigDecimal subtotal() {
        return items.stream()
            .map(LineItem::subtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal discountAmount() {
        BigDecimal sub = subtotal();
        if (sub.compareTo(DISCOUNT_THRESHOLD) > 0) {
            return sub.multiply(DISCOUNT_RATE)
                .setScale(SCALE, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal taxableAmount() {
        return subtotal().subtract(discountAmount())
            .setScale(SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal vatAmount() {
        return taxableAmount().multiply(VAT_RATE)
            .setScale(SCALE, RoundingMode.HALF_UP);
    }

    BigDecimal total() {
        return taxableAmount().add(vatAmount())
            .setScale(SCALE, RoundingMode.HALF_UP);
    }

    String receipt() {
        StringBuilder sb = new StringBuilder();
        sb.append("RECEIPT\n");
        sb.append("-------\n");
        
        for (LineItem item : items) {
            sb.append(String.format("%s x%d @ €%.2f = €%.2f%n",
                item.description(),
                item.quantity(),
                item.unitPrice(),
                item.subtotal()));
        }
        
        sb.append("-------\n");
        sb.append(String.format("Subtotal: €%.2f%n", subtotal()));
        
        BigDecimal discount = discountAmount();
        if (discount.compareTo(BigDecimal.ZERO) > 0) {
            sb.append(String.format("Discount (10%%): -€%.2f%n", discount));
        }
        
        sb.append(String.format("VAT (21%%): €%.2f%n", vatAmount()));
        sb.append(String.format("Total: €%.2f%n", total()));
        
        return sb.toString();
    }
}
```

```java
// Tests
class OrderTest {
    @org.junit.jupiter.api.Test
    void computesTotalWithoutDiscount() {
        List<LineItem> items = List.of(
            new LineItem("Widget", 2, new BigDecimal("25.00")),
            new LineItem("Gadget", 1, new BigDecimal("40.00"))
        );
        Order order = new Order(items);
        
        // Subtotal: 90; no discount (< 100); VAT: 90 * 0.21 = 18.90; Total: 108.90
        assert order.total().equals(new BigDecimal("108.90"));
    }
    
    @org.junit.jupiter.api.Test
    void appliesDiscountWhenThresholdExceeded() {
        List<LineItem> items = List.of(
            new LineItem("Widget", 5, new BigDecimal("25.00"))
        );
        Order order = new Order(items);
        
        // Subtotal: 125; discount: 12.50; taxable: 112.50; VAT: 23.625 → 23.63; Total: 136.13
        assert order.total().equals(new BigDecimal("136.13"));
    }
    
    @org.junit.jupiter.api.Test
    void validateRejectsNonPositiveQuantity() {
        List<LineItem> items = List.of(
            new LineItem("Widget", 0, new BigDecimal("10.00"))
        );
        try {
            new Order(items);
            assert false : "Should have thrown";
        } catch (IllegalArgumentException e) {
            assert e.getMessage().contains("Quantity must be positive");
        }
    }
    
    @org.junit.jupiter.api.Test
    void validateRejectsNegativePrice() {
        List<LineItem> items = List.of(
            new LineItem("Widget", 1, new BigDecimal("-10.00"))
        );
        try {
            new Order(items);
            assert false : "Should have thrown";
        } catch (IllegalArgumentException e) {
            assert e.getMessage().contains("Price cannot be negative");
        }
    }
    
    @org.junit.jupiter.api.Test
    void receiptsShowAllDetails() {
        List<LineItem> items = List.of(
            new LineItem("Widget", 3, new BigDecimal("35.00"))
        );
        Order order = new Order(items);
        String receipt = order.receipt();
        
        assert receipt.contains("Widget x3 @ €35.00 = €105.00");
        assert receipt.contains("Subtotal: €105.00");
        assert receipt.contains("Discount (10%): -€10.50");
        assert receipt.contains("VAT (21%):");
        assert receipt.contains("Total:");
    }
}
```

**Code structure:**
- `LineItem`: Immutable value object; one job is holding item data.
- `Order`: Encapsulates validation, computation, and receipt generation; named constants replace magic numbers; guard clauses flatten logic; BigDecimal ensures precise decimal arithmetic required for currency.
- Tests cover happy path (no discount, with discount), validation boundary cases, and receipt format.