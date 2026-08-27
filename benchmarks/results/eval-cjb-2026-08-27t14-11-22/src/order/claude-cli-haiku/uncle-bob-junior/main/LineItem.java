import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class LineItem {
    private final String description;
    private final BigDecimal price;
    private final int quantity;
    
    public LineItem(String description, BigDecimal price, int quantity) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description cannot be null or blank");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        this.description = description;
        this.price = price;
        this.quantity = quantity;
    }
    
    public BigDecimal getSubtotal() {
        return price.multiply(new BigDecimal(quantity));
    }
    
    public String getDescription() {
        return description;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public int getQuantity() {
        return quantity;
    }
}
