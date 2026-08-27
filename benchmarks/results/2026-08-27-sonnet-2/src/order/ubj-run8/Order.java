// Order.java
import java.util.List;

public final class Order {

    private final List<LineItem> lineItems;

    public Order(List<LineItem> lineItems) {
        if (lineItems == null || lineItems.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one line item");
        }
        this.lineItems = List.copyOf(lineItems);
    }

    public List<LineItem> lineItems() {
        return lineItems;
    }
}
