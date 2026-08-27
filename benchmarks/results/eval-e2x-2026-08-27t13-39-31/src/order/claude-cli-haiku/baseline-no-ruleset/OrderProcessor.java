public class OrderProcessor {
    public static void main(String[] args) {
        Order order = new Order();
        
        order.addItem(new LineItem("Laptop", 1, 65.50));
        order.addItem(new LineItem("Mouse", 2, 12.75));
        order.addItem(new LineItem("Keyboard", 1, 35.00));
        
        System.out.println(order.generateReceipt());
    }
}
