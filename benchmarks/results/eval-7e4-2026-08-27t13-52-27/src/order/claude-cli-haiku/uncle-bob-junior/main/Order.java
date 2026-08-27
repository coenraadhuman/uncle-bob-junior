import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
