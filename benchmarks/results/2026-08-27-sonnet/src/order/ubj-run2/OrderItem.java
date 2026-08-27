// OrderItem.java
import java.math.BigDecimal;
import java.util.Objects;

public record OrderItem(String description, int quantity, BigDecimal unitPrice) {

    public OrderItem {
        Objects.requireNonNull(description, "description must not be null");
        Objects.requireNonNull(unitPrice, "unitPrice must not be null");
    }

    BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
