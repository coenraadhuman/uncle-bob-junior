// LineItem.java
import java.math.BigDecimal;

public record LineItem(String description, int quantity, BigDecimal unitPrice) {

    public LineItem {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Line item description must not be blank");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Line item quantity must be positive: " + quantity);
        }
        if (unitPrice == null || unitPrice.signum() < 0) {
            throw new IllegalArgumentException("Line item unit price must not be negative: " + unitPrice);
        }
    }

    public BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
