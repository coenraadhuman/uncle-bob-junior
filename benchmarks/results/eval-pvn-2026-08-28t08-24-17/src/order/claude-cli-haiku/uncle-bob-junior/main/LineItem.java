import java.math.BigDecimal;
import java.util.Objects;

public final class LineItem {
    private final String description;
    private final int quantity;
    private final BigDecimal pricePerUnit;

    public LineItem(String description, int quantity, BigDecimal pricePerUnit) {
        Objects.requireNonNull(description, "description cannot be null");
        Objects.requireNonNull(pricePerUnit, "pricePerUnit cannot be null");
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        if (pricePerUnit.signum() < 0) {
            throw new IllegalArgumentException("pricePerUnit cannot be negative");
        }
        this.description = description;
        this.quantity = quantity;
        this.pricePerUnit = pricePerUnit;
    }

    public String description() {
        return description;
    }

    public int quantity() {
        return quantity;
    }

    public BigDecimal pricePerUnit() {
        return pricePerUnit;
    }

    public BigDecimal subtotal() {
        return pricePerUnit.multiply(BigDecimal.valueOf(quantity));
    }
}
