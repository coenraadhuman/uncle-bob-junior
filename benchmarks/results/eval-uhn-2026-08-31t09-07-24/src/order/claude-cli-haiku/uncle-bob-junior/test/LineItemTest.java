import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LineItemTest {
    @Test void rejectsBlankDescription() {
        assertThrows(IllegalArgumentException.class, () -> new LineItem("", 10.0, 1));
    }

    @Test void rejectsNegativePrice() {
        assertThrows(IllegalArgumentException.class, () -> new LineItem("Item", -0.01, 1));
    }

    @Test void rejectsNegativeQuantity() {
        assertThrows(IllegalArgumentException.class, () -> new LineItem("Item", 10.0, -1));
    }

    @Test void calculatesSubtotal() {
        assertEquals(7.50, new LineItem("Coffee", 2.50, 3).subtotal());
    }
}
