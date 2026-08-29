import java.math.BigDecimal;
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
