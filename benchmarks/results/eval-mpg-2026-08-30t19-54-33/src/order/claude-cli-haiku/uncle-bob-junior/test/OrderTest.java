import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.math.RoundingMode;
import static org.junit.jupiter.api.Assertions.*;

public class OrderTest {
    private static final BigDecimal PRICE_50 = new BigDecimal("50.00");
    private static final BigDecimal PRICE_60 = new BigDecimal("60.00");
    private static final BigDecimal PRICE_150 = new BigDecimal("150.00");

    @Test
    void orderUnder100EurosNoDiscount() {
        LineItem item = new LineItem("Widget", PRICE_50, 1);
        Order order = new Order(List.of(item));

        BigDecimal total = order.getFinalTotal();

        BigDecimal expected = PRICE_50.multiply(new BigDecimal("1.21"))
                .setScale(2, RoundingMode.HALF_UP);
        assertEquals(expected, total);
    }

    @Test
    void orderOver100EurosWithDiscount() {
        LineItem item = new LineItem("Widget", PRICE_150, 1);
        Order order = new Order(List.of(item));

        BigDecimal total = order.getFinalTotal();

        BigDecimal afterDiscount = PRICE_150.multiply(new BigDecimal("0.90"))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal expected = afterDiscount.multiply(new BigDecimal("1.21"))
                .setScale(2, RoundingMode.HALF_UP);
        assertEquals(expected, total);
    }

    @Test
    void discountAppliesAtThreshold() {
        LineItem item1 = new LineItem("Item A", PRICE_60, 1);
        LineItem item2 = new LineItem("Item B", PRICE_60, 1);
        Order order = new Order(List.of(item1, item2));

        BigDecimal total = order.getFinalTotal();

        BigDecimal subtotal = new BigDecimal("120.00");
        BigDecimal afterDiscount = subtotal.multiply(new BigDecimal("0.90"))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal expected = afterDiscount.multiply(new BigDecimal("1.21"))
                .setScale(2, RoundingMode.HALF_UP);
        assertEquals(expected, total);
    }

    @Test
    void lineItemRejectsNegativePrice() {
        assertThrows(IllegalArgumentException.class,
                () -> new LineItem("Widget", new BigDecimal("-10.00"), 1));
    }

    @Test
    void lineItemRejectsZeroPrice() {
        assertThrows(IllegalArgumentException.class,
                () -> new LineItem("Widget", BigDecimal.ZERO, 1));
    }

    @Test
    void lineItemRejectsNullDescription() {
        assertThrows(IllegalArgumentException.class,
                () -> new LineItem(null, PRICE_50, 1));
    }

    @Test
    void lineItemRejectsEmptyDescription() {
        assertThrows(IllegalArgumentException.class,
                () -> new LineItem("   ", PRICE_50, 1));
    }

    @Test
    void lineItemRejectsZeroQuantity() {
        assertThrows(IllegalArgumentException.class,
                () -> new LineItem("Widget", PRICE_50, 0));
    }

    @Test
    void lineItemRejectsNegativeQuantity() {
        assertThrows(IllegalArgumentException.class,
                () -> new LineItem("Widget", PRICE_50, -5));
    }

    @Test
    void receiptIncludesAllSections() {
        LineItem item = new LineItem("Laptop", new BigDecimal("1000.00"), 1);
        Order order = new Order(List.of(item));

        String receipt = order.generateReceipt();

        assertTrue(receipt.contains("RECEIPT"));
        assertTrue(receipt.contains("Laptop"));
        assertTrue(receipt.contains("Subtotal:"));
        assertTrue(receipt.contains("Discount (10%)"));
        assertTrue(receipt.contains("VAT (21%)"));
        assertTrue(receipt.contains("TOTAL:"));
    }

    @Test
    void receiptOmitsDiscountWhenNotApplied() {
        LineItem item = new LineItem("Widget", PRICE_50, 1);
        Order order = new Order(List.of(item));

        String receipt = order.generateReceipt();

        assertFalse(receipt.contains("Discount (10%)"));
    }

    @Test
    void multipleItemsWithQuantity() {
        LineItem item1 = new LineItem("Mouse", new BigDecimal("25.00"), 2);
        LineItem item2 = new LineItem("Keyboard", new BigDecimal("75.00"), 1);
        Order order = new Order(List.of(item1, item2));

        BigDecimal total = order.getFinalTotal();

        BigDecimal subtotal = new BigDecimal("125.00");
        BigDecimal afterDiscount = subtotal.multiply(new BigDecimal("0.90"))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal expected = afterDiscount.multiply(new BigDecimal("1.21"))
                .setScale(2, RoundingMode.HALF_UP);
        assertEquals(expected, total);
    }
}
