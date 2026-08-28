import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class OrderProcessor {
    public static void main(String[] args) {
        List<LineItem> items = new ArrayList<>();
        items.add(new LineItem("Laptop", new BigDecimal("899.99"), 1));
        items.add(new LineItem("Mouse", new BigDecimal("25.50"), 2));
        items.add(new LineItem("Keyboard", new BigDecimal("75.00"), 1));

        Order order = new Order(items);
        System.out.println(order.generateReceipt());
    }
}
