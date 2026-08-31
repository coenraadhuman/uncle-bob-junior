import java.util.ArrayList;
import java.util.List;

public class OrderProcessor {
    public static void main(String[] args) {
        List<LineItem> items = new ArrayList<>();
        items.add(new LineItem("Widget A", 25.00, 2));
        items.add(new LineItem("Widget B", 30.00, 2));
        items.add(new LineItem("Widget C", 15.00, 1));
        
        Order order = new Order(items);
        
        if (!order.validate()) {
            System.out.println("Invalid order items");
            return;
        }
        
        System.out.println(order.getReceipt());
    }
}
