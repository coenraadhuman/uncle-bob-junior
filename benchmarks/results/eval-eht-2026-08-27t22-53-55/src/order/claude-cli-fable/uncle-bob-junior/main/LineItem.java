import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/** One order line. Quantity must be positive, unit price non-negative, name non-blank. */
public record LineItem(String name, int quantity, BigDecimal unitPrice) {

    public LineItem {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Line item name must not be blank");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive, was " + quantity);
        }
        if (unitPrice == null || unitPrice.signum() < 0) {
            throw new IllegalArgumentException("Unit price must be non-negative");
        }
    }

    public BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
