public class Main {
    public static void main(String[] args) {
        Order order = new Order();
        order.addItem("Coffee", 5.50, 2);
        order.addItem("Sandwich", 8.75, 3);
        order.addItem("Juice", 3.25, 4);
        
        System.out.println(order.generateReceipt());
    }
}
