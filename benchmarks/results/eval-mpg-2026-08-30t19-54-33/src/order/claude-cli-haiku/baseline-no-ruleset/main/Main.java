public class Main {
    public static void main(String[] args) {
        Order order = new Order();
        order.addItem(new Order.LineItem("Coffee", 3.50, 2));
        order.addItem(new Order.LineItem("Sandwich", 8.00, 3));
        order.addItem(new Order.LineItem("Cake", 5.50, 10));
        
        System.out.println(order.generateReceipt());
    }
}
