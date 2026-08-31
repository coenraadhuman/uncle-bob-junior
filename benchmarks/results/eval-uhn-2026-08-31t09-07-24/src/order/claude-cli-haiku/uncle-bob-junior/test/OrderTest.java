import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OrderTest {
    @Test void rejectsEmptyOrder() {
        assertThrows(IllegalArgumentException.class, () -> new Order(List.of()));
    }

    @Test void rejectsNullOrder() {
        assertThrows(IllegalArgumentException.class, () -> new Order(null));
    }

    @Test void calculatesWithoutDiscount() {
        var order = new Order(List.of(new LineItem("Item", 50.0, 1)));
        assertEquals(50.0, order.preVatTotal());
        assertFalse(order.qualifiesForDiscount());
        assertEquals(50.0, order.subtotalAfterDiscount());
        assertEquals(10.5, order.vatAmount(), 0.01);
        assertEquals(60.5, order.total(), 0.01);
    }

    @Test void calculatesWithDiscount() {
        var order = new Order(List.of(new LineItem("Item", 50.0, 3)));
        assertEquals(150.0, order.preVatTotal());
        assertTrue(order.qualifiesForDiscount());
        assertEquals(15.0, order.discountAmount());
        assertEquals(135.0, order.subtotalAfterDiscount());
        assertEquals(28.35, order.vatAmount(), 0.01);
        assertEquals(163.35, order.total(), 0.01);
    }

    @Test void discountThresholdIsExclusive() {
        assertFalse(new Order(List.of(new LineItem("X", 100.0, 1))).qualifiesForDiscount());
        assertTrue(new Order(List.of(new LineItem("X", 100.01, 1))).qualifiesForDiscount());
    }

    @Test void generatesReceipt() {
        var order = new Order(List.of(
            new LineItem("Coffee", 2.50, 2),
            new LineItem("Sandwich", 5.00, 1)
        ));
        var receipt = order.receipt();
        assertTrue(receipt.contains("Coffee") && receipt.contains("TOTAL"));
    }

    @Test void receiptIncludesDiscountWhenApplicable() {
        var order = new Order(List.of(new LineItem("Item", 50.0, 3)));
        assertTrue(order.receipt().contains("Discount"));
    }
}
