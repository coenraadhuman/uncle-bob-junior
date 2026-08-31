public class Main {
    public static void main(String[] args) {
        Order order = new Order();
        order.addItem(new LineItem("Widget A", 5, 15.50));
        order.addItem(new LineItem("Widget B", 3, 20.00));
        order.addItem(new LineItem("Service", 1, 45.00));

        System.out.println(order.generateReceipt());
    }
}
