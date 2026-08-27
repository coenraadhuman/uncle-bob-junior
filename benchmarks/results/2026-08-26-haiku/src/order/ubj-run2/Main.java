public class Main {
    public static void main(String[] args) {
        List<LineItem> items = List.of(
            new LineItem("Laptop", 1, 799.99),
            new LineItem("Mouse", 2, 24.99),
            new LineItem("USB-C Cable", 3, 12.50)
        );
        
        OrderProcessor order = new OrderProcessor(items);
        System.out.println(order.generateReceipt());
    }
}
