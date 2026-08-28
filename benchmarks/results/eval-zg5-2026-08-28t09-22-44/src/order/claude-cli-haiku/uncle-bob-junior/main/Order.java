import java.util.List;
import java.util.Objects;

public class Order {
    private final List<LineItem> items;
    
    public Order(List<LineItem> items) {
        this.items = Objects.requireNonNull(items, "Items cannot be null");
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }
    }
    
    public List<LineItem> getItems() {
        return items;
    }
}
