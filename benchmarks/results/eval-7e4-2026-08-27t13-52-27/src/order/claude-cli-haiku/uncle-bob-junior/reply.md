I'll write a clean order processor with validation, discount logic, VAT calculation, and receipt formatting. I'll use `BigDecimal` for money to avoid floating-point precision errors.

**Assumptions:**
- Line items have a description, quantity, and unit price (all required, non-null)
- Quantities and prices must be positive
- Discount applies pre-VAT when subtotal > €100
- VAT is 21%, discount is 10%
- Receipt shows line items, subtotal, discount, VAT, and total

```java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

class LineItem {
  private final String description;
  private final int quantity;
  private final BigDecimal unitPrice;

  LineItem(String description, int quantity, BigDecimal unitPrice) {
    this.description = Objects.requireNonNull(description, "Description required");
    this.quantity = quantity;
    this.unitPrice = Objects.requireNonNull(unitPrice, "Unit price required");
    validate();
  }

  private void validate() {
    if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
    if (unitPrice.compareTo(BigDecimal.ZERO) <= 0)
      throw new IllegalArgumentException("Unit price must be positive");
  }

  BigDecimal lineTotal() {
    return unitPrice.multiply(BigDecimal.valueOf(quantity));
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
}

class Order {
  private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
  private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
  private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100");
  private static final int SCALE = 2;

  private final List<LineItem> items;

  Order(List<LineItem> items) {
    this.items = Collections.unmodifiableList(new ArrayList<>(
        Objects.requireNonNull(items, "Items required")));
    if (items.isEmpty()) throw new IllegalArgumentException("Order must have at least one item");
  }

  BigDecimal subtotal() {
    return items.stream()
        .map(LineItem::lineTotal)
        .reduce(BigDecimal.ZERO, BigDecimal::add)
        .setScale(SCALE, RoundingMode.HALF_UP);
  }

  BigDecimal discountAmount() {
    BigDecimal sub = subtotal();
    if (sub.compareTo(DISCOUNT_THRESHOLD) > 0) {
      return sub.multiply(DISCOUNT_RATE).setScale(SCALE, RoundingMode.HALF_UP);
    }
    return BigDecimal.ZERO;
  }

  BigDecimal subtotalAfterDiscount() {
    return subtotal().subtract(discountAmount()).setScale(SCALE, RoundingMode.HALF_UP);
  }

  BigDecimal vatAmount() {
    return subtotalAfterDiscount().multiply(VAT_RATE).setScale(SCALE, RoundingMode.HALF_UP);
  }

  BigDecimal total() {
    return subtotalAfterDiscount().add(vatAmount()).setScale(SCALE, RoundingMode.HALF_UP);
  }

  String receipt() {
    StringBuilder sb = new StringBuilder();
    sb.append("=== ORDER RECEIPT ===\n");
    for (LineItem item : items) {
      sb.append(String.format("%s x%d @ €%.2f = €%.2f\n",
          item.description(), item.quantity(), item.unitPrice(), item.lineTotal()));
    }
    sb.append("---\n");
    sb.append(String.format("Subtotal: €%.2f\n", subtotal()));
    if (discountAmount().compareTo(BigDecimal.ZERO) > 0) {
      sb.append(String.format("Discount (10%%): -€%.2f\n", discountAmount()));
    }
    sb.append(String.format("Subtotal after discount: €%.2f\n", subtotalAfterDiscount()));
    sb.append(String.format("VAT (21%%): €%.2f\n", vatAmount()));
    sb.append("---\n");
    sb.append(String.format("TOTAL: €%.2f\n", total()));
    return sb.toString();
  }

  List<LineItem> items() {
    return items;
  }
}
```

**Tests demonstrating behavior:**

```java
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class OrderTest {
  @Test
  void rejectsInvalidLineItems() {
    assertThrows(IllegalArgumentException.class, () ->
        new LineItem("Product", 0, new BigDecimal("10.00")));
    assertThrows(IllegalArgumentException.class, () ->
        new LineItem("Product", 1, new BigDecimal("-5.00")));
  }

  @Test
  void calculatesSubtotalCorrectly() {
    Order order = new Order(List.of(
        new LineItem("Milk", 2, new BigDecimal("3.50")),
        new LineItem("Bread", 1, new BigDecimal("2.80"))
    ));
    assertEquals(new BigDecimal("9.80"), order.subtotal());
  }

  @Test
  void appliesNoDiscountUnderThreshold() {
    Order order = new Order(List.of(
        new LineItem("Item", 10, new BigDecimal("5.00"))
    ));
    assertEquals(new BigDecimal("50.00"), order.subtotal());
    assertEquals(BigDecimal.ZERO, order.discountAmount());
  }

  @Test
  void appliesDiscountAboveThreshold() {
    Order order = new Order(List.of(
        new LineItem("Item", 20, new BigDecimal("5.50"))
    ));
    assertEquals(new BigDecimal("110.00"), order.subtotal());
    assertEquals(new BigDecimal("11.00"), order.discountAmount());
    assertEquals(new BigDecimal("99.00"), order.subtotalAfterDiscount());
  }

  @Test
  void calculatesVATAndTotal() {
    Order order = new Order(List.of(
        new LineItem("Item", 20, new BigDecimal("5.50"))
    ));
    assertEquals(new BigDecimal("20.79"), order.vatAmount());
    assertEquals(new BigDecimal("119.79"), order.total());
  }

  @Test
  void generatesReceiptString() {
    Order order = new Order(List.of(
        new LineItem("Milk", 2, new BigDecimal("3.50")),
        new LineItem("Cheese", 1, new BigDecimal("5.20"))
    ));
    String receipt = order.receipt();
    assertTrue(receipt.contains("Milk x2 @ €3.50 = €7.00"));
    assertTrue(receipt.contains("Cheese x1 @ €5.20 = €5.20"));
    assertTrue(receipt.contains("Subtotal: €12.40"));
    assertTrue(receipt.contains("TOTAL:"));
  }

  @Test
  void rejectsEmptyOrder() {
    assertThrows(IllegalArgumentException.class, () -> new Order(List.of()));
  }
}
```

**Usage example:**

```java
Order order = new Order(List.of(
    new LineItem("Laptop", 1, new BigDecimal("899.99")),
    new LineItem("Mouse", 2, new BigDecimal("25.50"))
));
System.out.println(order.receipt());
```

Cleaned: One class per responsibility (LineItem, Order). Validation at constructor boundaries. All monetary values use BigDecimal with explicit rounding. Guard clauses for null checks. Receipt formatting separated logically. Safe to change because each class has a single purpose and tests verify the discount, VAT, and receipt logic independently.