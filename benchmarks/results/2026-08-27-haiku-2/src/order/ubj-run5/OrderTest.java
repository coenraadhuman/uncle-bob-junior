public class OrderTest {
    @Test
    void validateOrderMustHaveItems() {
        assertThrows(IllegalArgumentException.class, () -> new Order(List.of()));
    }

    @Test
    void validateLineItemQuantityMustBePositive() {
        assertThrows(IllegalArgumentException.class, 
            () -> new LineItem("Item", 0, 1000));
    }

    @Test
    void validateLineItemPriceMustBePositive() {
        assertThrows(IllegalArgumentException.class, 
            () -> new LineItem("Item", 100, 0));
    }

    @Test
    void computeTotalWithoutDiscount() {
        List<LineItem> items = List.of(
            new LineItem("Pen", 100, 500)  // €5.00
        );
        Order order = new Order(items);
        
        assertEquals(500, order.subtotalInCents());
        assertEquals(0, order.discountInCents());
        assertEquals(105, order.vatInCents());
        assertEquals(605, order.totalInCents());
    }

    @Test
    void applyDiscountWhenSubtotalExceeds100Euros() {
        List<LineItem> items = List.of(
            new LineItem("Box", 1, 12000)  // €120.00
        );
        Order order = new Order(items);
        
        assertEquals(12000, order.subtotalInCents());
        assertEquals(1200, order.discountInCents());  // 10% of 12000
        assertEquals(10800, order.subtotalAfterDiscountInCents());
        assertEquals(2268, order.vatInCents());  // 21% of 10800
        assertEquals(13068, order.totalInCents());
    }

    @Test
    void multipleItems() {
        List<LineItem> items = List.of(
            new LineItem("Notebook", 2, 3500),  // €70.00
            new LineItem("Pen", 10, 150)        // €15.00
        );
        Order order = new Order(items);
        
        assertEquals(8500, order.subtotalInCents());
        assertEquals(850, order.discountInCents());
        assertEquals(1604, order.vatInCents());
        assertEquals(10254, order.totalInCents());
    }

    @Test
    void receiptFormatted() {
        List<LineItem> items = List.of(
            new LineItem("Coffee", 1, 350)  // €3.50
        );
        Order order = new Order(items);
        String receipt = order.receipt();
        
        assertThat(receipt)
            .contains("=== RECEIPT ===")
            .contains("Coffee: €3.50")
            .contains("Subtotal")
            .contains("VAT (21%)")
            .contains("TOTAL");
    }
}
