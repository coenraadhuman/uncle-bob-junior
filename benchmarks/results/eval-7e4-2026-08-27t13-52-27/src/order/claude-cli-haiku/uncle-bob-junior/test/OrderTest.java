import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class OrderTest {
  @Test
  void rejectsInvalidLineItems() {
    assertThrows(IllegalArgumentException.class, () ->
        new LineItem("Product", 0, new BigDecimal("10.00")));
    assertThrows(IllegalArgumentException.class, () ->
        new LineItem("Product", 1, new BigDecimal("-5.00")));
  }

  @Test
  void calculatesSubtotalCorrectly() {
    Order order = new Order(List.of(
        new LineItem("Milk", 2, new BigDecimal("3.50")),
        new LineItem("Bread", 1, new BigDecimal("2.80"))
    ));
    assertEquals(new BigDecimal("9.80"), order.subtotal());
  }

  @Test
  void appliesNoDiscountUnderThreshold() {
    Order order = new Order(List.of(
        new LineItem("Item", 10, new BigDecimal("5.00"))
    ));
    assertEquals(new BigDecimal("50.00"), order.subtotal());
    assertEquals(BigDecimal.ZERO, order.discountAmount());
  }

  @Test
  void appliesDiscountAboveThreshold() {
    Order order = new Order(List.of(
        new LineItem("Item", 20, new BigDecimal("5.50"))
    ));
    assertEquals(new BigDecimal("110.00"), order.subtotal());
    assertEquals(new BigDecimal("11.00"), order.discountAmount());
    assertEquals(new BigDecimal("99.00"), order.subtotalAfterDiscount());
  }

  @Test
  void calculatesVATAndTotal() {
    Order order = new Order(List.of(
        new LineItem("Item", 20, new BigDecimal("5.50"))
    ));
    assertEquals(new BigDecimal("20.79"), order.vatAmount());
    assertEquals(new BigDecimal("119.79"), order.total());
  }

  @Test
  void generatesReceiptString() {
    Order order = new Order(List.of(
        new LineItem("Milk", 2, new BigDecimal("3.50")),
        new LineItem("Cheese", 1, new BigDecimal("5.20"))
    ));
    String receipt = order.receipt();
    assertTrue(receipt.contains("Milk x2 @ €3.50 = €7.00"));
    assertTrue(receipt.contains("Cheese x1 @ €5.20 = €5.20"));
    assertTrue(receipt.contains("Subtotal: €12.40"));
    assertTrue(receipt.contains("TOTAL:"));
  }

  @Test
  void rejectsEmptyOrder() {
    assertThrows(IllegalArgumentException.class, () -> new Order(List.of()));
  }
}
