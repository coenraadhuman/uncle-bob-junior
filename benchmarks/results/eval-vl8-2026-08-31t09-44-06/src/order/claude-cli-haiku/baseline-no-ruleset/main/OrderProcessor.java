import java.math.BigDecimal;

public class OrderProcessor {
    public static void main(String[] args) {
        Order order = new Order();
        order.addLineItem("Widget A", new BigDecimal("50.00"));
        order.addLineItem("Widget B", new BigDecimal("60.00"));
        order.addLineItem("Service", new BigDecimal("15.00"));
        
        System.out.println(order.generateReceipt());
    }
}
