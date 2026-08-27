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
    Objects.requireNonNull(description, "Description cannot be null");
    Objects.requireNonNull(unitPrice, "Unit price cannot be null");
    if (quantity <= 0) {
      throw new IllegalArgumentException("Quantity must be positive");
    }
    if (unitPrice.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("Unit price cannot be negative");
    }
    this.description = description;
    this.quantity = quantity;
    this.unitPrice = unitPrice;
  }

  BigDecimal lineTotal() {
    return unitPrice.multiply(BigDecimal.valueOf(quantity));
  }

  @Override
  public String toString() {
    return String.format("%-30s %3d × €%8.2f = €%8.2f",
        description, quantity, unitPrice, lineTotal());
  }
}

class Order {
  private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
  private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
  private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100");

  private final List<LineItem> items;

  Order(List<LineItem> items) {
    Objects.requireNonNull(items, "Items cannot be null");
    if (items.isEmpty()) {
      throw new IllegalArgumentException("Order must contain at least one item");
    }
    this.items = new ArrayList<>(items);
  }

  private BigDecimal subtotal() {
    return items.stream()
        .map(LineItem::lineTotal)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private BigDecimal discountAmount(BigDecimal preVatTotal) {
    if (preVatTotal.compareTo(DISCOUNT_THRESHOLD) > 0) {
      return preVatTotal.multiply(DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP);
    }
    return BigDecimal.ZERO;
  }

  private BigDecimal vatAmount(BigDecimal taxableAmount) {
    return taxableAmount.multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);
  }

  String generateReceipt() {
    BigDecimal preVatTotal = subtotal();
    BigDecimal discount = discountAmount(preVatTotal);
    BigDecimal afterDiscount = preVatTotal.subtract(discount);
    BigDecimal vat = vatAmount(afterDiscount);
    BigDecimal total = afterDiscount.add(vat);

    StringBuilder receipt = new StringBuilder();
    receipt.append("═══════════════════════════════════════════════════\n");
    receipt.append("RECEIPT\n");
    receipt.append("═══════════════════════════════════════════════════\n\n");

    items.forEach(item -> receipt.append(item).append("\n"));

    receipt.append("───────────────────────────────────────────────────\n");
    receipt.append(String.format("Subtotal                           €%8.2f\n", preVatTotal));

    if (discount.compareTo(BigDecimal.ZERO) > 0) {
      receipt.append(String.format("Discount (10%%)                     €%8.2f\n", discount));
      receipt.append(String.format("After Discount                     €%8.2f\n", afterDiscount));
    }

    receipt.append(String.format("VAT (21%%)                          €%8.2f\n", vat));
    receipt.append("═══════════════════════════════════════════════════\n");
    receipt.append(String.format("Total                              €%8.2f\n", total));
    receipt.append("═══════════════════════════════════════════════════\n");

    return receipt.toString();
  }
}

// Test
public class OrderProcessing {
  public static void main(String[] args) {
    // Example 1: order under 100 euros (no discount)
    List<LineItem> items1 = List.of(
        new LineItem("Widget A", 2, new BigDecimal("25.00")),
        new LineItem("Widget B", 1, new BigDecimal("45.00"))
    );
    Order order1 = new Order(items1);
    System.out.println(order1.generateReceipt());

    // Example 2: order over 100 euros (10% discount applied)
    List<LineItem> items2 = List.of(
        new LineItem("Premium Widget", 5, new BigDecimal("25.50")),
        new LineItem("Service Fee", 1, new BigDecimal("10.00"))
    );
    Order order2 = new Order(items2);
    System.out.println(order2.generateReceipt());

    // Example 3: validation test
    try {
      new LineItem("Invalid", -1, new BigDecimal("10"));
    } catch (IllegalArgumentException e) {
      System.out.println("Validation error caught: " + e.getMessage());
    }
  }
}
