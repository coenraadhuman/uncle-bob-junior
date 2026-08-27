public class OrderProcessor {
    public static void main(String[] args) {
        Order order = new Order();
        
        order.addItem(new LineItem("Widget A", 3, 15.50));
        order.addItem(new LineItem("Widget B", 2, 28.00));
        order.addItem(new LineItem("Service", 1, 35.00));
        
        System.out.println(order.generateReceipt());
        System.out.println("Final Total: €" + String.format("%.2f", order.calculateTotal()));
    }
}
