public class Main {
    public static void main(String[] args) {
        Order order = new Order();

        try {
            order.addLineItem(new LineItem("Laptop", 1, 75.00));
            order.addLineItem(new LineItem("Mouse", 2, 15.00));
            order.addLineItem(new LineItem("Keyboard", 1, 45.00));

            System.out.println(order.generateReceipt());
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
