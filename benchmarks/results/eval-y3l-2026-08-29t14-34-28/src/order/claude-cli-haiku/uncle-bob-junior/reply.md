I'll write a complete order processor with validation, VAT calculation, conditional discount, and receipt formatting. Key assumptions: line items have description, price, and quantity; discount applies when pre-VAT subtotal exceeds 100 euros; prices use BigDecimal for precision; receipt displays all calculations.

```java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class OrderProcessor {
    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");

    public static class LineItem {
        private final String description;
        private final BigDecimal price;
        private final int quantity;

        public LineItem(String description, BigDecimal price, int quantity) {
            if (description == null || description.isBlank()) {
                throw new IllegalArgumentException("Description cannot be blank");
            }
            if (price == null || price.signum() < 0) {
                throw new IllegalArgumentException("Price cannot be negative");
            }
            if (quantity <= 0) {
                throw new IllegalArgumentException("Quantity must be positive");
            }
            this.description = description;
            this.price = price;
            this.quantity = quantity;
        }

        public BigDecimal lineTotal() {
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

    public static class OrderResult {
        private final List<LineItem> items;
        private final BigDecimal subtotal;
        private final BigDecimal discount;
        private final BigDecimal vat;
        private final BigDecimal total;

        private OrderResult(List<LineItem> items, BigDecimal subtotal, BigDecimal discount, BigDecimal vat, BigDecimal total) {
            this.items = items;
            this.subtotal = subtotal;
            this.discount = discount;
            this.vat = vat;
            this.total = total;
        }

        public String getReceipt() {
            StringBuilder receipt = new StringBuilder();
            receipt.append("=== RECEIPT ===\n");
            for (LineItem item : items) {
                receipt.append(String.format("%s x%d @ €%.2f = €%.2f\n",
                    item.getDescription(),
                    item.getQuantity(),
                    item.getPrice(),
                    item.lineTotal()));
            }
            receipt.append(String.format("\nSubtotal: €%.2f\n", subtotal));
            if (discount.signum() > 0) {
                receipt.append(String.format("Discount (10%%): -€%.2f\n", discount));
            }
            receipt.append(String.format("VAT (21%%): €%.2f\n", vat));
            receipt.append(String.format("TOTAL: €%.2f\n", total));
            return receipt.toString();
        }

        public BigDecimal getSubtotal() {
            return subtotal;
        }

        public BigDecimal getDiscount() {
            return discount;
        }

        public BigDecimal getVat() {
            return vat;
        }

        public BigDecimal getTotal() {
            return total;
        }
    }

    public static OrderResult process(List<LineItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        BigDecimal subtotal = items.stream()
            .map(LineItem::lineTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discountAmount = subtotal.compareTo(DISCOUNT_THRESHOLD) > 0
            ? subtotal.multiply(DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;

        BigDecimal discountedSubtotal = subtotal.subtract(discountAmount);
        BigDecimal vat = discountedSubtotal.multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = discountedSubtotal.add(vat);

        return new OrderResult(items, subtotal, discountAmount, vat, total);
    }
}
```

```java
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;

public class OrderProcessorTest {

    @Test
    void validatesLineItemDescription() {
        assertThrows(IllegalArgumentException.class,
            () -> new OrderProcessor.LineItem("", new BigDecimal("10.00"), 1));
        assertThrows(IllegalArgumentException.class,
            () -> new OrderProcessor.LineItem(null, new BigDecimal("10.00"), 1));
    }

    @Test
    void validatesLineItemPrice() {
        assertThrows(IllegalArgumentException.class,
            () -> new OrderProcessor.LineItem("Item", new BigDecimal("-5.00"), 1));
        assertThrows(IllegalArgumentException.class,
            () -> new OrderProcessor.LineItem("Item", null, 1));
    }

    @Test
    void validatesLineItemQuantity() {
        assertThrows(IllegalArgumentException.class,
            () -> new OrderProcessor.LineItem("Item", new BigDecimal("10.00"), 0));
        assertThrows(IllegalArgumentException.class,
            () -> new OrderProcessor.LineItem("Item", new BigDecimal("10.00"), -1));
    }

    @Test
    void calculatesLineTotalCorrectly() {
        var item = new OrderProcessor.LineItem("Widget", new BigDecimal("25.00"), 3);
        assertEquals(new BigDecimal("75.00"), item.lineTotal());
    }

    @Test
    void requiresAtLeastOneItem() {
        assertThrows(IllegalArgumentException.class,
            () -> OrderProcessor.process(Collections.emptyList()));
        assertThrows(IllegalArgumentException.class,
            () -> OrderProcessor.process(null));
    }

    @Test
    void computesVatWithoutDiscountWhenBelowThreshold() {
        var items = Arrays.asList(
            new OrderProcessor.LineItem("Item A", new BigDecimal("50.00"), 1)
        );
        var result = OrderProcessor.process(items);

        assertEquals(new BigDecimal("50.00"), result.getSubtotal());
        assertEquals(BigDecimal.ZERO, result.getDiscount());
        assertEquals(new BigDecimal("10.50"), result.getVat());
        assertEquals(new BigDecimal("60.50"), result.getTotal());
    }

    @Test
    void appliesDiscountAboveThreshold() {
        var items = Arrays.asList(
            new OrderProcessor.LineItem("Item A", new BigDecimal("60.00"), 1),
            new OrderProcessor.LineItem("Item B", new BigDecimal("50.00"), 1)
        );
        var result = OrderProcessor.process(items);

        assertEquals(new BigDecimal("110.00"), result.getSubtotal());
        assertEquals(new BigDecimal("11.00"), result.getDiscount());
        assertEquals(new BigDecimal("20.79"), result.getVat());
        assertEquals(new BigDecimal("119.79"), result.getTotal());
    }

    @Test
    void noDiscountAtThresholdBoundary() {
        var items = Arrays.asList(
            new OrderProcessor.LineItem("Item", new BigDecimal("100.00"), 1)
        );
        var result = OrderProcessor.process(items);

        assertEquals(new BigDecimal("100.00"), result.getSubtotal());
        assertEquals(BigDecimal.ZERO, result.getDiscount());
        assertEquals(new BigDecimal("21.00"), result.getVat());
        assertEquals(new BigDecimal("121.00"), result.getTotal());
    }

    @Test
    void producesReceiptWithLineItems() {
        var items = Arrays.asList(
            new OrderProcessor.LineItem("Coffee", new BigDecimal("3.50"), 2),
            new OrderProcessor.LineItem("Sandwich", new BigDecimal("8.00"), 1)
        );
        var result = OrderProcessor.process(items);
        String receipt = result.getReceipt();

        assertTrue(receipt.contains("Coffee x2 @ €3.50 = €7.00"));
        assertTrue(receipt.contains("Sandwich x1 @ €8.00 = €8.00"));
        assertTrue(receipt.contains("Subtotal: €15.00"));
        assertTrue(receipt.contains("VAT (21%)"));
        assertTrue(receipt.contains("TOTAL:"));
    }

    @Test
    void receiptIncludesDiscountWhenApplied() {
        var items = Arrays.asList(
            new OrderProcessor.LineItem("Premium Item", new BigDecimal("105.00"), 1)
        );
        var result = OrderProcessor.process(items);
        String receipt = result.getReceipt();

        assertTrue(receipt.contains("Discount (10%): -€10.50"));
    }
}
```

**Cleaned:** validation at construction boundaries, named constants replace magic numbers, immutable result object, receipt generation isolated. Safe to change because each class has one responsibility and invalid states are unrepresentable by the type system.