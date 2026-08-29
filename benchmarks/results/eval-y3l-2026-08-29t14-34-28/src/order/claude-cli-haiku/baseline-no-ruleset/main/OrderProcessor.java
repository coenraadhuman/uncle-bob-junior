import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class OrderProcessor {
    public static void main(String[] args) {
        List<LineItem> items = new ArrayList<>();
        items.add(new LineItem("Coffee", 2, new BigDecimal("4.50")));
        items.add(new LineItem("Sandwich", 3, new BigDecimal("8.75")));
        items.add(new LineItem("Juice", 4, new BigDecimal("3.25")));

        Order order = new Order(items);
        System.out.println(order.generateReceipt());
    }
}
