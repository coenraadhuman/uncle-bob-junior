public class OrderExample {
    public static void main(String[] args) {
        List<LineItem> items = List.of(
            new LineItem("Laptop", 899.99, 1),
            new LineItem("Mouse", 25.50, 2),
            new LineItem("Keyboard", 79.99, 1)
        );
        
        Order order = new Order(items);
        System.out.println(order.getReceipt());
    }
}
