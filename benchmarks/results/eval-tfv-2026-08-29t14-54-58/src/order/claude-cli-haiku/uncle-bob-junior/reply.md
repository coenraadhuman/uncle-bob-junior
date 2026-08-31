```java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class LineItem {
    private final String description;
    private final int quantity;
    private final BigDecimal unitPrice;

    LineItem(String description, int quantity, BigDecimal unitPrice) {
        this.description = Objects.requireNonNull(description);
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (unitPrice.signum() < 0) {
            throw new IllegalArgumentException("Unit price cannot be negative");
        }
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    BigDecimal subtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    String description() {
        return description;
    }

    int quantity() {
        return quantity;
    }
}

class Order {
    private static final BigDecimal VAT_RATE = BigDecimal.valueOf(0.21);
    private static final BigDecimal DISCOUNT_RATE = BigDecimal.valueOf(0.10);
    private static final BigDecimal DISCOUNT_THRESHOLD = BigDecimal.valueOf(100);

    private final List<LineItem> items;
    private final BigDecimal subtotal;
    private final BigDecimal discountAmount;
    private final BigDecimal discountedSubtotal;
    private final BigDecimal vat;
    private final BigDecimal total;

    Order(List<LineItem> items) {
        this.items = new ArrayList<>(Objects.requireNonNull(items));
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one item");
        }
        this.subtotal = computeSubtotal();
        this.discountAmount = computeDiscount();
        this.discountedSubtotal = subtotal.subtract(discountAmount);
        this.vat = discountedSubtotal.multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);
        this.total = discountedSubtotal.add(vat).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal computeSubtotal() {
        BigDecimal sum = BigDecimal.ZERO;
        for (LineItem item : items) {
            sum = sum.add(item.subtotal());
        }
        return sum.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal computeDiscount() {
        if (subtotal.compareTo(DISCOUNT_THRESHOLD) > 0) {
            return subtotal.multiply(DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    String receipt() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ORDER RECEIPT ===\n");
        for (LineItem item : items) {
            sb.append(String.format("%s x%d: €%.2f\n", 
                item.description(), item.quantity(), item.subtotal()));
        }
        sb.append(String.format("\nSubtotal: €%.2f\n", subtotal));
        if (discountAmount.signum() > 0) {
            sb.append(String.format("Discount (10%%): -€%.2f\n", discountAmount));
            sb.append(String.format("After Discount: €%.2f\n", discountedSubtotal));
        }
        sb.append(String.format("VAT (21%%): €%.2f\n", vat));
        sb.append(String.format("Total: €%.2f\n", total));
        return sb.toString();
    }

    BigDecimal getTotal() { return total; }
    BigDecimal getSubtotal() { return subtotal; }
    BigDecimal getDiscount() { return discountAmount; }
    BigDecimal getVat() { return vat; }
}

class OrderProcessorTest {
    void testNoDiscountBelowThreshold() {
        List<LineItem> items = List.of(
            new LineItem("Widget", 2, BigDecimal.valueOf(25.00)),
            new LineItem("Gadget", 1, BigDecimal.valueOf(30.00))
        );
        Order order = new Order(items);
        assert order.getSubtotal().equals(BigDecimal.valueOf(80.00));
        assert order.getDiscount().equals(BigDecimal.ZERO);
        assert order.getVat().equals(BigDecimal.valueOf(16.80));
        assert order.getTotal().equals(BigDecimal.valueOf(96.80));
    }

    void testDiscountAppliedAboveThreshold() {
        List<LineItem> items = List.of(
            new LineItem("Premium Widget", 5, BigDecimal.valueOf(25.00))
        );
        Order order = new Order(items);
        assert order.getSubtotal().equals(BigDecimal.valueOf(125.00));
        assert order.getDiscount().equals(BigDecimal.valueOf(12.50));
        assert order.getVat().equals(BigDecimal.valueOf(23.63));
        assert order.getTotal().equals(BigDecimal.valueOf(136.13));
    }

    void testReceiptFormatting() {
        List<LineItem> items = List.of(
            new LineItem("Item A", 1, BigDecimal.valueOf(50.00))
        );
        Order order = new Order(items);
        String receipt = order.receipt();
        assert receipt.contains("Item A x1: €50.00");
        assert receipt.contains("Subtotal: €50.00");
        assert !receipt.contains("Discount");
        assert receipt.contains("VAT (21%): €10.50");
        assert receipt.contains("Total: €60.50");
    }

    void testValidationEmptyOrder() {
        try {
            new Order(new ArrayList<>());
            assert false;
        } catch (IllegalArgumentException e) {
            assert e.getMessage().contains("at least one item");
        }
    }

    void testValidationNegativeQuantity() {
        try {
            new LineItem("Item", 0, BigDecimal.TEN);
            assert false;
        } catch (IllegalArgumentException e) {
            assert e.getMessage().contains("Quantity");
        }
    }

    void testValidationNegativePrice() {
        try {
            new LineItem("Item", 1, BigDecimal.valueOf(-10));
            assert false;
        } catch (IllegalArgumentException e) {
            assert e.getMessage().contains("price");
        }
    }

    public static void main(String[] args) {
        OrderProcessorTest test = new OrderProcessorTest();
        test.testNoDiscountBelowThreshold();
        test.testDiscountAppliedAboveThreshold();
        test.testReceiptFormatting();
        test.testValidationEmptyOrder();
        test.testValidationNegativeQuantity();
        test.testValidationNegativePrice();
        System.out.println("All tests passed!");
    }
}
```

**Assumptions**: Discount (10%) applies to pre-VAT subtotal when it exceeds €100.00; VAT is calculated on the discounted amount; all values use `BigDecimal` with HALF_UP rounding to 2 decimal places; quantities must be positive integers and prices non-negative.

**Cleaned**: LineItem validates its own invariants; Order computes total, discount, and VAT in immutable fields; receipt builds a formatted string with conditional discount display. Safe to extend with new discount tiers or additional fees since each calculation is isolated.