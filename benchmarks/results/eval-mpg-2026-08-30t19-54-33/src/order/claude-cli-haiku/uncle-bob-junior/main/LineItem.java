import java.math.BigDecimal;

public class LineItem {
    private final String description;
    private final BigDecimal unitPrice;
    private final int quantity;

    public LineItem(String description, BigDecimal unitPrice, int quantity) {
        validateDescription(description);
        validateUnitPrice(unitPrice);
        validateQuantity(quantity);
        this.description = description;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    private void validateDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be null or empty");
        }
    }

    private void validateUnitPrice(BigDecimal unitPrice) {
        if (unitPrice == null || unitPrice.signum() <= 0) {
            throw new IllegalArgumentException("Unit price must be positive");
        }
    }

    private void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
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
