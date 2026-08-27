// Tests
class OrderTest {
    @org.junit.jupiter.api.Test
    void computesTotalWithoutDiscount() {
        List<LineItem> items = List.of(
            new LineItem("Widget", 2, new BigDecimal("25.00")),
            new LineItem("Gadget", 1, new BigDecimal("40.00"))
        );
        Order order = new Order(items);
        
        // Subtotal: 90; no discount (< 100); VAT: 90 * 0.21 = 18.90; Total: 108.90
        assert order.total().equals(new BigDecimal("108.90"));
    }
    
    @org.junit.jupiter.api.Test
    void appliesDiscountWhenThresholdExceeded() {
        List<LineItem> items = List.of(
            new LineItem("Widget", 5, new BigDecimal("25.00"))
        );
        Order order = new Order(items);
        
        // Subtotal: 125; discount: 12.50; taxable: 112.50; VAT: 23.625 → 23.63; Total: 136.13
        assert order.total().equals(new BigDecimal("136.13"));
    }
    
    @org.junit.jupiter.api.Test
    void validateRejectsNonPositiveQuantity() {
        List<LineItem> items = List.of(
            new LineItem("Widget", 0, new BigDecimal("10.00"))
        );
        try {
            new Order(items);
            assert false : "Should have thrown";
        } catch (IllegalArgumentException e) {
            assert e.getMessage().contains("Quantity must be positive");
        }
    }
    
    @org.junit.jupiter.api.Test
    void validateRejectsNegativePrice() {
        List<LineItem> items = List.of(
            new LineItem("Widget", 1, new BigDecimal("-10.00"))
        );
        try {
            new Order(items);
            assert false : "Should have thrown";
        } catch (IllegalArgumentException e) {
            assert e.getMessage().contains("Price cannot be negative");
        }
    }
    
    @org.junit.jupiter.api.Test
    void receiptsShowAllDetails() {
        List<LineItem> items = List.of(
            new LineItem("Widget", 3, new BigDecimal("35.00"))
        );
        Order order = new Order(items);
        String receipt = order.receipt();
        
        assert receipt.contains("Widget x3 @ €35.00 = €105.00");
        assert receipt.contains("Subtotal: €105.00");
        assert receipt.contains("Discount (10%): -€10.50");
        assert receipt.contains("VAT (21%):");
        assert receipt.contains("Total:");
    }
}
