// Order.java
import java.util.List;
import java.util.Objects;

public record Order(List<LineItem> items) {

    public Order {
        items = List.copyOf(Objects.requireNonNull(items, "items must not be null"));
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one line item");
        }
    }
}
