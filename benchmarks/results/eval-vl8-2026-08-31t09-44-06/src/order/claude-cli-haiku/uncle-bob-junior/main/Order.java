import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class Order {
    private final List<LineItem> items;
    
    Order(List<LineItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }
        this.items = Collections.unmodifiableList(new ArrayList<>(items));
    }
    
    List<LineItem> items() {
        return items;
    }
}
