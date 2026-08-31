I'll write clean, testable Java code to process orders with validation, VAT, and conditional discount.

**Assumptions:**
- Line items have description, quantity (positive integer), and unit price
- Discount (10%) applies when pre-VAT subtotal **exceeds** €100 (not equals)
- Discount is applied before VAT (standard EU approach)
- Using BigDecimal for financial precision with HALF_UP rounding
- VAT rate is 21% (Netherlands standard)

```java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class LineItem {
    private final String description;
    private final int quantity;
    private final BigDecimal pricePerUnit;

    public LineItem(String description, int quantity, BigDecimal pricePerUnit) {
        Objects.requireNonNull(description, "description cannot be null");
        Objects.requireNonNull(pricePerUnit, "pricePerUnit cannot be null");
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        if (pricePerUnit.signum() < 0) {
            throw new IllegalArgumentException("pricePerUnit cannot be negative");
        }
        this.description = description;
        this.quantity = quantity;
        this.pricePerUnit = pricePerUnit;
    }

    public String description() {
        return description;
    }

    public int quantity() {
        return quantity;
    }

    public BigDecimal pricePerUnit() {
        return pricePerUnit;
    }

    public BigDecimal subtotal() {
        return pricePerUnit.multiply(BigDecimal.valueOf(quantity));
    }
}

public final class OrderCalculation {
    private final BigDecimal subtotal;
    private final BigDecimal discount;
    private final BigDecimal discountedTotal;
    private final BigDecimal vat;
    private final BigDecimal total;

    public OrderCalculation(BigDecimal subtotal, BigDecimal discount, BigDecimal discountedTotal,
                           BigDecimal vat, BigDecimal total) {
        this.subtotal = subtotal;
        this.discount = discount;
        this.discountedTotal = discountedTotal;
        this.vat = vat;
        this.total = total;
    }

    BigDecimal subtotal() { return subtotal; }
    BigDecimal discount() { return discount; }
    BigDecimal discountedTotal() { return discountedTotal; }
    BigDecimal vat() { return vat; }
    BigDecimal total() { return total; }
}

public final class Order {
    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100");
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    private final List<LineItem> lineItems;

    public Order(List<LineItem> lineItems) {
        Objects.requireNonNull(lineItems, "lineItems cannot be null");
        if (lineItems.isEmpty()) {
            throw new IllegalArgumentException("order must have at least one line item");
        }
        this.lineItems = Collections.unmodifiableList(new ArrayList<>(lineItems));
    }

    public Receipt generateReceipt() {
        BigDecimal subtotal = calculateSubtotal();
        BigDecimal discount = calculateDiscount(subtotal);
        BigDecimal discountedTotal = subtotal.subtract(discount);
        BigDecimal vat = calculateVat(discountedTotal);
        BigDecimal total = discountedTotal.add(vat);

        OrderCalculation calculation = new OrderCalculation(subtotal, discount, discountedTotal, vat, total);
        return new Receipt(lineItems, calculation);
    }

    private BigDecimal calculateSubtotal() {
        return lineItems.stream()
            .map(LineItem::subtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateDiscount(BigDecimal subtotal) {
        if (subtotal.compareTo(DISCOUNT_THRESHOLD) > 0) {
            return subtotal.multiply(DISCOUNT_RATE).setScale(2, ROUNDING);
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal calculateVat(BigDecimal discountedTotal) {
        return discountedTotal.multiply(VAT_RATE).setScale(2, ROUNDING);
    }
}

public final class Receipt {
    private final List<LineItem> lineItems;
    private final OrderCalculation calculation;

    public Receipt(List<LineItem> lineItems, OrderCalculation calculation) {
        this.lineItems = Collections.unmodifiableList(lineItems);
        this.calculation = calculation;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== RECEIPT ===\n");

        for (LineItem item : lineItems) {
            sb.append(String.format("%s x%d @ €%.2f = €%.2f\n",
                item.description(), item.quantity(), item.pricePerUnit(), item.subtotal()));
        }

        sb.append(String.format("\nSubtotal: €%.2f\n", calculation.subtotal()));
        if (calculation.discount().signum() > 0) {
            sb.append(String.format("Discount (10%%): -€%.2f\n", calculation.discount()));
            sb.append(String.format("After discount: €%.2f\n", calculation.discountedTotal()));
        }
        sb.append(String.format("VAT (21%%): €%.2f\n", calculation.vat()));
        sb.append(String.format("\nTOTAL: €%.2f\n", calculation.total()));

        return sb.toString();
    }
}
```

**Tests:**

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class OrderProcessingTest {

    @Test
    public void lineItemValidatesNonNullDescription() {
        assertThrows(NullPointerException.class, () ->
            new LineItem(null, 1, new BigDecimal("10")));
    }

    @Test
    public void lineItemValidatesNonNullPrice() {
        assertThrows(NullPointerException.class, () ->
            new LineItem("item", 1, null));
    }

    @Test
    public void lineItemValidatesPositiveQuantity() {
        assertThrows(IllegalArgumentException.class, () ->
            new LineItem("item", 0, new BigDecimal("10")));
        assertThrows(IllegalArgumentException.class, () ->
            new LineItem("item", -1, new BigDecimal("10")));
    }

    @Test
    public void lineItemValidatesNonNegativePrice() {
        assertThrows(IllegalArgumentException.class, () ->
            new LineItem("item", 1, new BigDecimal("-10")));
    }

    @Test
    public void lineItemCalculatesSubtotal() {
        LineItem item = new LineItem("item", 5, new BigDecimal("20"));
        assertEquals(new BigDecimal("100"), item.subtotal());
    }

    @Test
    public void orderRequiresNonEmptyLineItems() {
        assertThrows(IllegalArgumentException.class, () ->
            new Order(new ArrayList<>()));
    }

    @Test
    public void noDiscountWhenSubtotalAtThreshold() {
        Order order = new Order(Arrays.asList(
            new LineItem("item", 1, new BigDecimal("100"))
        ));
        String receipt = order.generateReceipt().toString();

        assertFalse(receipt.contains("Discount"));
        assertTrue(receipt.contains("Subtotal: €100.00"));
        assertTrue(receipt.contains("TOTAL: €121.00"));
    }

    @Test
    public void appliesDiscountWhenSubtotalExceeds100() {
        Order order = new Order(Arrays.asList(
            new LineItem("item", 1, new BigDecimal("150"))
        ));
        String receipt = order.generateReceipt().toString();

        assertTrue(receipt.contains("Discount (10%): -€15.00"));
        assertTrue(receipt.contains("After discount: €135.00"));
        assertTrue(receipt.contains("VAT (21%): €28.35"));
        assertTrue(receipt.contains("TOTAL: €163.35"));
    }

    @Test
    public void calculatesVatOnDiscountedAmount() {
        Order order = new Order(Arrays.asList(
            new LineItem("coffee", 10, new BigDecimal("15"))
        ));
        String receipt = order.generateReceipt().toString();

        assertTrue(receipt.contains("Discount (10%): -€15.00"));
        assertTrue(receipt.contains("After discount: €135.00"));
        assertTrue(receipt.contains("VAT (21%): €28.35"));
    }

    @Test
    public void handlesMultipleLineItems() {
        Order order = new Order(Arrays.asList(
            new LineItem("coffee", 2, new BigDecimal("5.50")),
            new LineItem("pastry", 1, new BigDecimal("3.00"))
        ));
        String receipt = order.generateReceipt().toString();

        assertTrue(receipt.contains("coffee x2 @ €5.50 = €11.00"));
        assertTrue(receipt.contains("pastry x1 @ €3.00 = €3.00"));
        assertTrue(receipt.contains("Subtotal: €14.00"));
        assertFalse(receipt.contains("Discount"));
        assertTrue(receipt.contains("VAT (21%): €2.94"));
        assertTrue(receipt.contains("TOTAL: €16.94"));
    }

    @Test
    public void edgeCaseJustAboveThreshold() {
        Order order = new Order(Arrays.asList(
            new LineItem("item", 1, new BigDecimal("100.01"))
        ));
        String receipt = order.generateReceipt().toString();

        assertTrue(receipt.contains("Discount (10%): -€10.00"));
    }
}
```

**Cleaned:** Separated concerns into LineItem (value, validation), OrderCalculation (data holder for results), Order (business logic), and Receipt (formatting). All validations at boundaries, named constants for rates/thresholds, BigDecimal for financial precision. Safe to change because each class has one job and calculations are isolated.