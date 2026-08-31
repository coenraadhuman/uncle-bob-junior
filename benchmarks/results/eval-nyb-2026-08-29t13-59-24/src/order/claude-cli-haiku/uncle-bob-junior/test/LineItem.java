import java.math.BigDecimal;
import java.util.Objects;

class LineItem {
    private final String description;
    private final int quantity;
    private final BigDecimal unitPrice;
    
    LineItem(String description, int quantity, BigDecimal unitPrice) {
        this.description = Objects.requireNonNull(description);
        this.quantity = quantity;
        this.unitPrice = Objects.requireNonNull(unitPrice);
    }
    
    boolean isValid() {
        return !description.isEmpty() && quantity > 0 && unitPrice.signum() > 0;
    }
    
    BigDecimal getLineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
    
    String getDescription() {
        return description;
    }
}
