import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

class OrderProcessorTest {
    void testNoDiscountBelowThreshold() {
        List<LineItem> items = List.of(
            new LineItem("Widget", 2, BigDecimal.valueOf(25.00)),
            new LineItem("Gadget", 1, BigDecimal.valueOf(30.00))
        );
        Order order = new Order(items);
        assert order.getSubtotal().equals(BigDecimal.valueOf(80.00));
        assert order.getDiscount().equals(BigDecimal.ZERO);
        assert order.getVat().equals(BigDecimal.valueOf(16.80));
        assert order.getTotal().equals(BigDecimal.valueOf(96.80));
    }

    void testDiscountAppliedAboveThreshold() {
        List<LineItem> items = List.of(
            new LineItem("Premium Widget", 5, BigDecimal.valueOf(25.00))
        );
        Order order = new Order(items);
        assert order.getSubtotal().equals(BigDecimal.valueOf(125.00));
        assert order.getDiscount().equals(BigDecimal.valueOf(12.50));
        assert order.getVat().equals(BigDecimal.valueOf(23.63));
        assert order.getTotal().equals(BigDecimal.valueOf(136.13));
    }

    void testReceiptFormatting() {
        List<LineItem> items = List.of(
            new LineItem("Item A", 1, BigDecimal.valueOf(50.00))
        );
        Order order = new Order(items);
        String receipt = order.receipt();
        assert receipt.contains("Item A x1: €50.00");
        assert receipt.contains("Subtotal: €50.00");
        assert !receipt.contains("Discount");
        assert receipt.contains("VAT (21%): €10.50");
        assert receipt.contains("Total: €60.50");
    }

    void testValidationEmptyOrder() {
        try {
            new Order(new ArrayList<>());
            assert false;
        } catch (IllegalArgumentException e) {
            assert e.getMessage().contains("at least one item");
        }
    }

    void testValidationNegativeQuantity() {
        try {
            new LineItem("Item", 0, BigDecimal.TEN);
            assert false;
        } catch (IllegalArgumentException e) {
            assert e.getMessage().contains("Quantity");
        }
    }

    void testValidationNegativePrice() {
        try {
            new LineItem("Item", 1, BigDecimal.valueOf(-10));
            assert false;
        } catch (IllegalArgumentException e) {
            assert e.getMessage().contains("price");
        }
    }

    public static void main(String[] args) {
        OrderProcessorTest test = new OrderProcessorTest();
        test.testNoDiscountBelowThreshold();
        test.testDiscountAppliedAboveThreshold();
        test.testReceiptFormatting();
        test.testValidationEmptyOrder();
        test.testValidationNegativeQuantity();
        test.testValidationNegativePrice();
        System.out.println("All tests passed!");
    }
}
