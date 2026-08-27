// Test usage and verification
public class OrderProcessorTest {
    public static void main(String[] args) {
        testValidOrder();
        testOrderWithDiscount();
        testInvalidItem();
        testEmptyOrder();
    }

    private static void testValidOrder() {
        System.out.println("--- Test: Valid Order (no discount) ---");
        List<LineItem> items = List.of(
            new LineItem("Coffee", 5.0, 2),
            new LineItem("Croissant", 3.5, 3)
        );
        Order order = new Order(items);
        System.out.println(order.produceReceipt());
        System.out.println();
    }

    private static void testOrderWithDiscount() {
        System.out.println("--- Test: Order with Discount (>€100) ---");
        List<LineItem> items = List.of(
            new LineItem("Laptop Monitor", 120.0, 1),
            new LineItem("USB Cable", 8.50, 2)
        );
        Order order = new Order(items);
        System.out.println(order.produceReceipt());
        System.out.println();
    }

    private static void testInvalidItem() {
        System.out.println("--- Test: Invalid Item (negative price) ---");
        try {
            List<LineItem> items = List.of(new LineItem("Item", -5.0, 2));
            Order order = new Order(items);
            order.produceReceipt();
        } catch (IllegalArgumentException e) {
            System.out.println("✓ Caught: " + e.getMessage());
        }
        System.out.println();
    }

    private static void testEmptyOrder() {
        System.out.println("--- Test: Empty Order ---");
        try {
            Order order = new Order(List.of());
            order.produceReceipt();
        } catch (IllegalArgumentException e) {
            System.out.println("✓ Caught: " + e.getMessage());
        }
    }
}
