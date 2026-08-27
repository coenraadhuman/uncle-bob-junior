// Example usage
public class OrderExample {
    public static void main(String[] args) {
        List<LineItem> items = List.of(
            new LineItem("Widget A", 2, new BigDecimal("45.50")),
            new LineItem("Widget B", 1, new BigDecimal("30.00")),
            new LineItem("Service Fee", 1, new BigDecimal("35.75"))
        );
        
        Order order = new Order(items);
        System.out.println(order.generateReceipt());
    }
}
