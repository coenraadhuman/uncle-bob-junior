public class Main {
    public static void main(String[] args) {
        Order order = new Order();
        order.addLineItem("Widget A", 45.50, 2);
        order.addLineItem("Widget B", 12.99, 3);

        System.out.println(order.processOrder());
    }
}
