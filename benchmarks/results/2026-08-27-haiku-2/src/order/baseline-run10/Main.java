import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // Example 1: Order exceeding 100 euros (gets 10% discount)
        List<LineItem> items1 = new ArrayList<>();
        items1.add(new LineItem("Laptop", 1, 80.00));
        items1.add(new LineItem("Mouse", 2, 15.00));
        items1.add(new LineItem("USB Cable", 3, 5.00));

        OrderProcessor processor1 = new OrderProcessor(items1);
        System.out.println("=== Order 1 (with discount) ===");
        System.out.println(processor1.generateReceipt());

        // Example 2: Order below 100 euros (no discount)
        List<LineItem> items2 = new ArrayList<>();
        items2.add(new LineItem("Book", 2, 20.00));
        items2.add(new LineItem("Pen", 5, 2.00));

        OrderProcessor processor2 = new OrderProcessor(items2);
        System.out.println("\n=== Order 2 (no discount) ===");
        System.out.println(processor2.generateReceipt());

        // Example 3: Invalid order (demonstrates validation)
        try {
            List<LineItem> items3 = new ArrayList<>();
            items3.add(new LineItem("Invalid Item", -1, 50.00));
            OrderProcessor processor3 = new OrderProcessor(items3);
            processor3.generateReceipt();
        } catch (IllegalArgumentException e) {
            System.out.println("\n=== Order 3 (validation error) ===");
            System.out.println("Error: " + e.getMessage());
        }
    }
}
