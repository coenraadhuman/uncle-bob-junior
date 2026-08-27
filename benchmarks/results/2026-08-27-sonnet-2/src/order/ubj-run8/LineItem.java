// LineItem.java
import java.math.BigDecimal;

public final class LineItem {

    private final String description;
    private final BigDecimal unitPrice;
    private final int quantity;

    public LineItem(String description, BigDecimal unitPrice, int quantity) {
        validate(description, unitPrice, quantity);
        this.description = description;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    private static void validate(String description, BigDecimal unitPrice, int quantity) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description must not be blank");
        }
        if (unitPrice == null || unitPrice.signum() < 0) {
            throw new IllegalArgumentException("Unit price must not be negative");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
    }

    public BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public String description() {
        return description;
    }

    public BigDecimal unitPrice() {
        return unitPrice;
    }

    public int quantity() {
        return quantity;
    }
}
