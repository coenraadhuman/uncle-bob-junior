import java.math.BigDecimal;
import java.math.RoundingMode;

public record LineItem(String description, BigDecimal unitPrice, int quantity) {

    public LineItem {
        if (description == null || description.isBlank()) {
            throw new InvalidOrderException("Line item description must not be blank");
        }
        if (unitPrice == null || unitPrice.signum() <= 0) {
            throw new InvalidOrderException("Unit price must be greater than zero: " + description);
        }
        if (quantity <= 0) {
            throw new InvalidOrderException("Quantity must be greater than zero: " + description);
        }
    }

    BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
