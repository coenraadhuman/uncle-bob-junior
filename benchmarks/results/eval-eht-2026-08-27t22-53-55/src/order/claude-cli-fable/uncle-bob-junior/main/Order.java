import java.util.List;

/** A validated, immutable order: at least one line item. */
public record Order(List<LineItem> items) {

    public Order {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one line item");
        }
        items = List.copyOf(items);
    }
}
