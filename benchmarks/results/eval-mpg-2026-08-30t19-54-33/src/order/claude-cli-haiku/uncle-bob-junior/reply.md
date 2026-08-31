I'll write a complete Java order processing solution. Here are my assumptions:

**Assumptions:**
- Line items have description, unit price (EUR), and quantity
- Prices use `BigDecimal` for precision (2 decimal places)
- Discount applies to pre-VAT subtotal; VAT is then calculated on the discounted amount
- Invalid items (null/empty description, non-positive price/quantity) throw `IllegalArgumentException`
- Receipt is a formatted string showing items, calculations, and total

---

```java
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class LineItem {
    private final String description;
    private final BigDecimal unitPrice;
    private final int quantity;

    public LineItem(String description, BigDecimal unitPrice, int quantity) {
        validateDescription(description);
        validateUnitPrice(unitPrice);
        validateQuantity(quantity);
        this.description = description;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    private void validateDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be null or empty");
        }
    }

    private void validateUnitPrice(BigDecimal unitPrice) {
        if (unitPrice == null || unitPrice.signum() <= 0) {
            throw new IllegalArgumentException("Unit price must be positive");
        }
    }

    private void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
    }

    public BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }
}

public class Order {
    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final int CURRENCY_SCALE = 2;

    private final List<LineItem> items;

    public Order(List<LineItem> items) {
        this.items = new ArrayList<>(items);
    }

    public String generateReceipt() {
        StringBuilder receipt = new StringBuilder();
        appendItemsSection(receipt);
        appendTotalsSection(receipt);
        return receipt.toString();
    }

    private void appendItemsSection(StringBuilder receipt) {
        receipt.append("RECEIPT\n");
        receipt.append("-------\n");
        for (LineItem item : items) {
            receipt.append(formatLineItem(item)).append("\n");
        }
    }

    private String formatLineItem(LineItem item) {
        BigDecimal lineTotal = item.lineTotal();
        return String.format("%s x%d @ €%.2f = €%.2f",
                item.getDescription(),
                item.getQuantity(),
                item.getUnitPrice(),
                lineTotal);
    }

    private void appendTotalsSection(StringBuilder receipt) {
        receipt.append("-------\n");
        BigDecimal subtotal = computeSubtotal();
        receipt.append(String.format("Subtotal: €%.2f\n", subtotal));

        if (shouldApplyDiscount(subtotal)) {
            BigDecimal discount = computeDiscount(subtotal);
            subtotal = subtotal.subtract(discount);
            receipt.append(String.format("Discount (10%%): -€%.2f\n", discount));
            receipt.append(String.format("Subtotal after discount: €%.2f\n", subtotal));
        }

        BigDecimal vat = computeVat(subtotal);
        receipt.append(String.format("VAT (21%%): €%.2f\n", vat));
        BigDecimal total = subtotal.add(vat);
        receipt.append(String.format("TOTAL: €%.2f\n", total));
    }

    private BigDecimal computeSubtotal() {
        return items.stream()
                .map(LineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private boolean shouldApplyDiscount(BigDecimal subtotal) {
        return subtotal.compareTo(DISCOUNT_THRESHOLD) > 0;
    }

    private BigDecimal computeDiscount(BigDecimal subtotal) {
        return subtotal.multiply(DISCOUNT_RATE)
                .setScale(CURRENCY_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal computeVat(BigDecimal taxableAmount) {
        return taxableAmount.multiply(VAT_RATE)
                .setScale(CURRENCY_SCALE, RoundingMode.HALF_UP);
    }

    public BigDecimal getFinalTotal() {
        BigDecimal subtotal = computeSubtotal();
        if (shouldApplyDiscount(subtotal)) {
            subtotal = subtotal.subtract(computeDiscount(subtotal));
        }
        BigDecimal vat = computeVat(subtotal);
        return subtotal.add(vat).setScale(CURRENCY_SCALE, RoundingMode.HALF_UP);
    }
}
```

---

```java
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.math.RoundingMode;
import static org.junit.jupiter.api.Assertions.*;

public class OrderTest {
    private static final BigDecimal PRICE_50 = new BigDecimal("50.00");
    private static final BigDecimal PRICE_60 = new BigDecimal("60.00");
    private static final BigDecimal PRICE_150 = new BigDecimal("150.00");

    @Test
    void orderUnder100EurosNoDiscount() {
        LineItem item = new LineItem("Widget", PRICE_50, 1);
        Order order = new Order(List.of(item));

        BigDecimal total = order.getFinalTotal();

        BigDecimal expected = PRICE_50.multiply(new BigDecimal("1.21"))
                .setScale(2, RoundingMode.HALF_UP);
        assertEquals(expected, total);
    }

    @Test
    void orderOver100EurosWithDiscount() {
        LineItem item = new LineItem("Widget", PRICE_150, 1);
        Order order = new Order(List.of(item));

        BigDecimal total = order.getFinalTotal();

        BigDecimal afterDiscount = PRICE_150.multiply(new BigDecimal("0.90"))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal expected = afterDiscount.multiply(new BigDecimal("1.21"))
                .setScale(2, RoundingMode.HALF_UP);
        assertEquals(expected, total);
    }

    @Test
    void discountAppliesAtThreshold() {
        LineItem item1 = new LineItem("Item A", PRICE_60, 1);
        LineItem item2 = new LineItem("Item B", PRICE_60, 1);
        Order order = new Order(List.of(item1, item2));

        BigDecimal total = order.getFinalTotal();

        BigDecimal subtotal = new BigDecimal("120.00");
        BigDecimal afterDiscount = subtotal.multiply(new BigDecimal("0.90"))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal expected = afterDiscount.multiply(new BigDecimal("1.21"))
                .setScale(2, RoundingMode.HALF_UP);
        assertEquals(expected, total);
    }

    @Test
    void lineItemRejectsNegativePrice() {
        assertThrows(IllegalArgumentException.class,
                () -> new LineItem("Widget", new BigDecimal("-10.00"), 1));
    }

    @Test
    void lineItemRejectsZeroPrice() {
        assertThrows(IllegalArgumentException.class,
                () -> new LineItem("Widget", BigDecimal.ZERO, 1));
    }

    @Test
    void lineItemRejectsNullDescription() {
        assertThrows(IllegalArgumentException.class,
                () -> new LineItem(null, PRICE_50, 1));
    }

    @Test
    void lineItemRejectsEmptyDescription() {
        assertThrows(IllegalArgumentException.class,
                () -> new LineItem("   ", PRICE_50, 1));
    }

    @Test
    void lineItemRejectsZeroQuantity() {
        assertThrows(IllegalArgumentException.class,
                () -> new LineItem("Widget", PRICE_50, 0));
    }

    @Test
    void lineItemRejectsNegativeQuantity() {
        assertThrows(IllegalArgumentException.class,
                () -> new LineItem("Widget", PRICE_50, -5));
    }

    @Test
    void receiptIncludesAllSections() {
        LineItem item = new LineItem("Laptop", new BigDecimal("1000.00"), 1);
        Order order = new Order(List.of(item));

        String receipt = order.generateReceipt();

        assertTrue(receipt.contains("RECEIPT"));
        assertTrue(receipt.contains("Laptop"));
        assertTrue(receipt.contains("Subtotal:"));
        assertTrue(receipt.contains("Discount (10%)"));
        assertTrue(receipt.contains("VAT (21%)"));
        assertTrue(receipt.contains("TOTAL:"));
    }

    @Test
    void receiptOmitsDiscountWhenNotApplied() {
        LineItem item = new LineItem("Widget", PRICE_50, 1);
        Order order = new Order(List.of(item));

        String receipt = order.generateReceipt();

        assertFalse(receipt.contains("Discount (10%)"));
    }

    @Test
    void multipleItemsWithQuantity() {
        LineItem item1 = new LineItem("Mouse", new BigDecimal("25.00"), 2);
        LineItem item2 = new LineItem("Keyboard", new BigDecimal("75.00"), 1);
        Order order = new Order(List.of(item1, item2));

        BigDecimal total = order.getFinalTotal();

        BigDecimal subtotal = new BigDecimal("125.00");
        BigDecimal afterDiscount = subtotal.multiply(new BigDecimal("0.90"))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal expected = afterDiscount.multiply(new BigDecimal("1.21"))
                .setScale(2, RoundingMode.HALF_UP);
        assertEquals(expected, total);
    }
}
```

Cleaned: `LineItem` validates at construction; `Order` isolates subtotal, discount, and VAT calculations in single-purpose methods; `BigDecimal` ensures precision; receipt generation separates item formatting from totals calculation. Safe to change because validation prevents invalid order state, calculations are centralized, and tests cover boundary cases (at/above discount threshold, empty description, invalid amounts).