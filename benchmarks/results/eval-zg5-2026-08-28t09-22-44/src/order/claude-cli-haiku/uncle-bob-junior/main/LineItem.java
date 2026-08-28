import java.math.BigDecimal;
import java.util.Objects;

public class LineItem {
    private final String description;
    private final int quantity;
    private final BigDecimal unitPrice;
    
    public LineItem(String description, int quantity, BigDecimal unitPrice) {
        this.description = Objects.requireNonNull(description, "Description cannot be null");
        this.quantity = validateQuantity(quantity);
        this.unitPrice = validateUnitPrice(unitPrice);
    }
    
    private int validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        return quantity;
    }
    
    private BigDecimal validateUnitPrice(BigDecimal unitPrice) {
        Objects.requireNonNull(unitPrice, "Unit price cannot be null");
        if (unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Unit price must be positive");
        }
        return unitPrice;
    }
    
    public BigDecimal lineTotal() {
        return unitPrice.multiply(new BigDecimal(quantity));
    }
    
    public String getDescription() {
        return description;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public BigDecimal getUnitPrice() {
        return unitPrice;
    }
}
