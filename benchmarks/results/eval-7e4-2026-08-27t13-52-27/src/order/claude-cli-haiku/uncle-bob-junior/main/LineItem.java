import java.math.BigDecimal;
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
