import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LineItem {
    private final String description;
    private final BigDecimal unitPrice;
    private final int quantity;

    public LineItem(String description, BigDecimal unitPrice, int quantity) {
        this.description = Objects.requireNonNull(description, "Description required");
        this.unitPrice = Objects.requireNonNull(unitPrice, "Unit price required");
        this.quantity = quantity;
        validate();
    }

    private void validate() {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive, got " + quantity);
        }
        if (unitPrice.signum() < 0) {
            throw new IllegalArgumentException("Unit price cannot be negative");
        }
    }

    public BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }
}
