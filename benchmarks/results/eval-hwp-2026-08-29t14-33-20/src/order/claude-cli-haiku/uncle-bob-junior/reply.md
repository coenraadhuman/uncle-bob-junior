```java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class Order {
    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final int CURRENCY_SCALE = 2;

    private List<LineItem> lineItems;

    public Order(List<LineItem> lineItems) {
        this.lineItems = validateLineItems(lineItems);
    }

    private List<LineItem> validateLineItems(List<LineItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one line item");
        }
        for (LineItem item : items) {
            if (item.quantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be positive");
            }
            if (item.unitPrice().signum() <= 0) {
                throw new IllegalArgumentException("Unit price must be positive");
            }
        }
        return items;
    }

    public String generateReceipt() {
        BigDecimal subtotal = calculateSubtotal();
        BigDecimal discount = calculateDiscount(subtotal);
        BigDecimal afterDiscount = subtotal.subtract(discount);
        BigDecimal vat = calculateVat(afterDiscount);
        BigDecimal total = afterDiscount.add(vat);

        return formatReceipt(subtotal, discount, afterDiscount, vat, total);
    }

    private BigDecimal calculateSubtotal() {
        return lineItems.stream()
            .map(item -> item.unitPrice().multiply(new BigDecimal(item.quantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(CURRENCY_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateDiscount(BigDecimal subtotal) {
        if (subtotal.compareTo(DISCOUNT_THRESHOLD) <= 0) {
            return BigDecimal.ZERO;
        }
        return subtotal.multiply(DISCOUNT_RATE)
            .setScale(CURRENCY_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateVat(BigDecimal amount) {
        return amount.multiply(VAT_RATE)
            .setScale(CURRENCY_SCALE, RoundingMode.HALF_UP);
    }

    private String formatReceipt(BigDecimal subtotal, BigDecimal discount, 
                                  BigDecimal afterDiscount, BigDecimal vat, BigDecimal total) {
        StringBuilder receipt = new StringBuilder();
        receipt.append("=== RECEIPT ===\n");
        
        for (LineItem item : lineItems) {
            BigDecimal lineTotal = item.unitPrice().multiply(new BigDecimal(item.quantity()))
                .setScale(CURRENCY_SCALE, RoundingMode.HALF_UP);
            receipt.append(String.format("%s x%d @ €%.2f = €%.2f\n", 
                item.description(), item.quantity(), item.unitPrice(), lineTotal));
        }
        
        receipt.append(String.format("\nSubtotal: €%.2f\n", subtotal));
        if (discount.signum() > 0) {
            receipt.append(String.format("Discount (10%%): -€%.2f\n", discount));
        }
        receipt.append(String.format("VAT (21%%): €%.2f\n", vat));
        receipt.append(String.format("Total: €%.2f\n", total));
        
        return receipt.toString();
    }
}

record LineItem(String description, int quantity, BigDecimal unitPrice) {
}
```

```java
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class OrderTest {
    @Test
    void rejectsEmptyOrder() {
        assertThrows(IllegalArgumentException.class, () -> new Order(List.of()));
    }

    @Test
    void rejectsZeroQuantity() {
        assertThrows(IllegalArgumentException.class, () -> 
            new Order(List.of(new LineItem("Item", 0, new BigDecimal("10.00")))));
    }

    @Test
    void rejectsNegativePrice() {
        assertThrows(IllegalArgumentException.class, () -> 
            new Order(List.of(new LineItem("Item", 1, new BigDecimal("-5.00")))));
    }

    @Test
    void computesTotalWithoutDiscount() {
        Order order = new Order(List.of(
            new LineItem("Apple", 5, new BigDecimal("10.00"))
        ));
        
        String receipt = order.generateReceipt();
        
        assertTrue(receipt.contains("Subtotal: €50.00"));
        assertFalse(receipt.contains("Discount"));
        assertTrue(receipt.contains("VAT (21%): €10.50"));
        assertTrue(receipt.contains("Total: €60.50"));
    }

    @Test
    void appliesDiscountWhenSubtotalExceeds100() {
        Order order = new Order(List.of(
            new LineItem("Item A", 5, new BigDecimal("25.00")),
            new LineItem("Item B", 3, new BigDecimal("10.00"))
        ));
        
        String receipt = order.generateReceipt();
        
        assertTrue(receipt.contains("Subtotal: €155.00"));
        assertTrue(receipt.contains("Discount (10%): -€15.50"));
        assertTrue(receipt.contains("VAT (21%): €29.30"));
        assertTrue(receipt.contains("Total: €168.80"));
    }

    @Test
    void doesNotApplyDiscountAt100() {
        Order order = new Order(List.of(
            new LineItem("Item", 10, new BigDecimal("10.00"))
        ));
        
        String receipt = order.generateReceipt();
        
        assertTrue(receipt.contains("Subtotal: €100.00"));
        assertFalse(receipt.contains("Discount"));
        assertTrue(receipt.contains("VAT (21%): €21.00"));
        assertTrue(receipt.contains("Total: €121.00"));
    }
}
```

**Assumptions:** Discount applies strictly above €100, not at €100 itself; line items require description, positive quantity, and positive price; monetary calculations use `BigDecimal` with half-up rounding to 2 decimals; discount is applied before VAT (to the pre-VAT subtotal).

Cleaned: `Order` separates validation, calculation, and formatting with single-job functions; named constants replace all meaningful literals; tests cover happy path (no discount, with discount) and boundary (exact threshold, invalid items).