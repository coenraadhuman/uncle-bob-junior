I'll create clean, tested Java code for order processing. **Assumptions**: LineItem has description, unit price, and quantity (int); discount is strictly when pre-VAT > 100 (not ≥); VAT applies to the discounted subtotal; receipt includes items and all calculations.

```java
import java.util.*;

record LineItem(String description, double unitPrice, int quantity) {
    public LineItem {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description cannot be empty");
        }
        if (unitPrice < 0) {
            throw new IllegalArgumentException("Unit price cannot be negative");
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
    }

    double subtotal() {
        return unitPrice * quantity;
    }
}

class Order {
    private static final double VAT_RATE = 0.21;
    private static final double DISCOUNT_RATE = 0.10;
    private static final double DISCOUNT_THRESHOLD = 100.0;

    private final List<LineItem> items;

    public Order(List<LineItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }
        this.items = new ArrayList<>(items);
    }

    public double preVatTotal() {
        return items.stream().mapToDouble(LineItem::subtotal).sum();
    }

    public boolean qualifiesForDiscount() {
        return preVatTotal() > DISCOUNT_THRESHOLD;
    }

    public double discountAmount() {
        return qualifiesForDiscount() ? preVatTotal() * DISCOUNT_RATE : 0.0;
    }

    public double subtotalAfterDiscount() {
        return preVatTotal() - discountAmount();
    }

    public double vatAmount() {
        return subtotalAfterDiscount() * VAT_RATE;
    }

    public double total() {
        return subtotalAfterDiscount() + vatAmount();
    }

    public String receipt() {
        StringBuilder sb = new StringBuilder();
        sb.append("========== RECEIPT ==========\n");
        for (LineItem item : items) {
            sb.append(String.format("%-20s %2d x €%7.2f = €%8.2f\n",
                item.description(), item.quantity(), item.unitPrice(), item.subtotal()));
        }
        sb.append("-----------------------------\n");
        sb.append(String.format("Subtotal:                 €%8.2f\n", preVatTotal()));
        if (qualifiesForDiscount()) {
            sb.append(String.format("Discount (10%%):          -€%8.2f\n", discountAmount()));
        }
        sb.append(String.format("VAT (21%%):                €%8.2f\n", vatAmount()));
        sb.append("=============================\n");
        sb.append(String.format("TOTAL:                    €%8.2f\n", total()));
        return sb.toString();
    }
}
```

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LineItemTest {
    @Test void rejectsBlankDescription() {
        assertThrows(IllegalArgumentException.class, () -> new LineItem("", 10.0, 1));
    }

    @Test void rejectsNegativePrice() {
        assertThrows(IllegalArgumentException.class, () -> new LineItem("Item", -0.01, 1));
    }

    @Test void rejectsNegativeQuantity() {
        assertThrows(IllegalArgumentException.class, () -> new LineItem("Item", 10.0, -1));
    }

    @Test void calculatesSubtotal() {
        assertEquals(7.50, new LineItem("Coffee", 2.50, 3).subtotal());
    }
}

class OrderTest {
    @Test void rejectsEmptyOrder() {
        assertThrows(IllegalArgumentException.class, () -> new Order(List.of()));
    }

    @Test void rejectsNullOrder() {
        assertThrows(IllegalArgumentException.class, () -> new Order(null));
    }

    @Test void calculatesWithoutDiscount() {
        var order = new Order(List.of(new LineItem("Item", 50.0, 1)));
        assertEquals(50.0, order.preVatTotal());
        assertFalse(order.qualifiesForDiscount());
        assertEquals(50.0, order.subtotalAfterDiscount());
        assertEquals(10.5, order.vatAmount(), 0.01);
        assertEquals(60.5, order.total(), 0.01);
    }

    @Test void calculatesWithDiscount() {
        var order = new Order(List.of(new LineItem("Item", 50.0, 3)));
        assertEquals(150.0, order.preVatTotal());
        assertTrue(order.qualifiesForDiscount());
        assertEquals(15.0, order.discountAmount());
        assertEquals(135.0, order.subtotalAfterDiscount());
        assertEquals(28.35, order.vatAmount(), 0.01);
        assertEquals(163.35, order.total(), 0.01);
    }

    @Test void discountThresholdIsExclusive() {
        assertFalse(new Order(List.of(new LineItem("X", 100.0, 1))).qualifiesForDiscount());
        assertTrue(new Order(List.of(new LineItem("X", 100.01, 1))).qualifiesForDiscount());
    }

    @Test void generatesReceipt() {
        var order = new Order(List.of(
            new LineItem("Coffee", 2.50, 2),
            new LineItem("Sandwich", 5.00, 1)
        ));
        var receipt = order.receipt();
        assertTrue(receipt.contains("Coffee") && receipt.contains("TOTAL"));
    }

    @Test void receiptIncludesDiscountWhenApplicable() {
        var order = new Order(List.of(new LineItem("Item", 50.0, 3)));
        assertTrue(order.receipt().contains("Discount"));
    }
}
```

**Code structure**: `LineItem` is a record (immutable value type) with validation; `Order` computes line subtotals, applies discount only when pre-VAT > 100, adds 21% VAT to discounted amount, and formats a formatted receipt. All constants named, methods under 10 statements, tests cover happy path and edges (threshold, discount logic, receipt format).