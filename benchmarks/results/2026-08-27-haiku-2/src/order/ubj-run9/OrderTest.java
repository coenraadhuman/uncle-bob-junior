import java.math.BigDecimal;
import java.util.List;

public class OrderTest {
    public static void main(String[] args) {
        testSmallOrder();
        testLargeOrderWithDiscount();
        testInvalidLineItem();
    }

    private static void testSmallOrder() {
        List<LineItem> items = List.of(
                new LineItem("Coffee", new BigDecimal("2.50"), 2),
                new LineItem("Pastry", new BigDecimal("3.00"), 1)
        );
        Order order = new Order(items);

        assert order.total().equals(new BigDecimal("7.83")) : "Small order total incorrect";
        System.out.println("✓ Small order (no discount):\n" + order.generateReceipt() + "\n");
    }

    private static void testLargeOrderWithDiscount() {
        List<LineItem> items = List.of(
                new LineItem("Widget", new BigDecimal("50.00"), 2),
                new LineItem("Gadget", new BigDecimal("30.00"), 1)
        );
        Order order = new Order(items);

        BigDecimal subtotal = new BigDecimal("130.00");
        BigDecimal discounted = subtotal.subtract(subtotal.multiply(new BigDecimal("0.10")));
        BigDecimal expected = discounted.multiply(new BigDecimal("1.21"));

        assert order.total().equals(expected) : "Large order total incorrect";
        System.out.println("✓ Large order (10% discount applied):\n" + order.generateReceipt() + "\n");
    }

    private static void testInvalidLineItem() {
        try {
            new LineItem("Bad Item", new BigDecimal("10.00"), 0);
            assert false : "Should reject zero quantity";
        } catch (IllegalArgumentException e) {
            System.out.println("✓ Correctly rejected zero quantity: " + e.getMessage());
        }
    }
}
