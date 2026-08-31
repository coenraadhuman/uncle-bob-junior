Here's a clean, complete Java solution:

```java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class LineItem {
    private final String description;
    private final int quantity;
    private final BigDecimal unitPrice;

    public LineItem(String description, int quantity, BigDecimal unitPrice) {
        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    void validate() {
        if (description == null || description.isEmpty()) {
            throw new IllegalArgumentException("Item description cannot be empty");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Unit price cannot be negative");
        }
    }

    BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity))
            .setScale(2, RoundingMode.HALF_UP);
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

public class OrderProcessor {
    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");

    public String processOrder(List<LineItem> items) {
        validateItems(items);

        BigDecimal subtotal = computeSubtotal(items);
        BigDecimal discount = computeDiscount(subtotal);
        BigDecimal afterDiscount = subtotal.subtract(discount);
        BigDecimal vat = afterDiscount.multiply(VAT_RATE)
            .setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = afterDiscount.add(vat);

        return formatReceipt(items, subtotal, discount, vat, total);
    }

    private void validateItems(List<LineItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }
        items.forEach(LineItem::validate);
    }

    private BigDecimal computeSubtotal(List<LineItem> items) {
        return items.stream()
            .map(LineItem::lineTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal computeDiscount(BigDecimal subtotal) {
        if (subtotal.compareTo(DISCOUNT_THRESHOLD) > 0) {
            return subtotal.multiply(DISCOUNT_RATE)
                .setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    private String formatReceipt(List<LineItem> items, BigDecimal subtotal,
                                 BigDecimal discount, BigDecimal vat, BigDecimal total) {
        StringBuilder receipt = new StringBuilder();
        receipt.append("=== RECEIPT ===\n");

        items.forEach(item -> receipt.append(formatLineItem(item)));

        receipt.append("-----------\n");
        receipt.append(formatLine("Subtotal", subtotal));

        if (discount.compareTo(BigDecimal.ZERO) > 0) {
            receipt.append(formatLine("Discount (10%)", discount.negate()));
        }

        receipt.append(formatLine("VAT (21%)", vat));
        receipt.append("-----------\n");
        receipt.append(formatLine("TOTAL", total));

        return receipt.toString();
    }

    private String formatLineItem(LineItem item) {
        return String.format("%s x%d @ €%.2f = €%.2f%n",
            item.getDescription(),
            item.getQuantity(),
            item.getUnitPrice(),
            item.lineTotal());
    }

    private String formatLine(String label, BigDecimal amount) {
        return String.format("%-20s €%.2f%n", label, amount);
    }
}

// Usage example:
// OrderProcessor processor = new OrderProcessor();
// List<LineItem> items = List.of(
//     new LineItem("Laptop", 1, new BigDecimal("600.00")),
//     new LineItem("Mouse", 2, new BigDecimal("25.00"))
// );
// String receipt = processor.processOrder(items);
// System.out.println(receipt);
```

**Key design decisions:**
- `BigDecimal` for money (no floating-point rounding errors)
- Guard clauses for validation; exceptions on invalid input
- One method per responsibility: validate, compute subtotal, compute discount, format
- Named constants for VAT/discount rates and threshold
- `HALF_UP` rounding for all monetary calculations (standard EU practice)
- Discount applied before VAT (pre-VAT total > 100€)