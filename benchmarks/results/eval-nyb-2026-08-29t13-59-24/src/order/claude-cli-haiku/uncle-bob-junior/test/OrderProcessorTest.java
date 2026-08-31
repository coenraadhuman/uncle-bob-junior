import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

class OrderProcessorTest {
    void testOrderUnder100NoDiscount() {
        List<LineItem> items = new ArrayList<>();
        items.add(new LineItem("Widget", 1, new BigDecimal("50.00")));
        
        Receipt receipt = new OrderProcessor().processOrder(items);
        assert receipt.getSubtotal().equals(new BigDecimal("50.00"));
        assert receipt.getDiscount().equals(BigDecimal.ZERO);
        assert receipt.getVat().equals(new BigDecimal("10.50"));
        assert receipt.getTotal().equals(new BigDecimal("60.50"));
    }
    
    void testOrderOver100WithDiscount() {
        List<LineItem> items = new ArrayList<>();
        items.add(new LineItem("Widget", 2, new BigDecimal("60.00")));
        
        Receipt receipt = new OrderProcessor().processOrder(items);
        assert receipt.getSubtotal().equals(new BigDecimal("120.00"));
        assert receipt.getDiscount().equals(new BigDecimal("12.00"));
        assert receipt.getVat().equals(new BigDecimal("22.68"));
        assert receipt.getTotal().equals(new BigDecimal("130.68"));
    }
    
    void testMultipleLineItems() {
        List<LineItem> items = new ArrayList<>();
        items.add(new LineItem("Item A", 2, new BigDecimal("30.00")));
        items.add(new LineItem("Item B", 3, new BigDecimal("25.00")));
        
        Receipt receipt = new OrderProcessor().processOrder(items);
        assert receipt.getSubtotal().equals(new BigDecimal("135.00"));
        assert receipt.getDiscount().equals(new BigDecimal("13.50"));
        assert receipt.getVat().equals(new BigDecimal("25.41"));
        assert receipt.getTotal().equals(new BigDecimal("146.91"));
    }
    
    void testOrderAtThreshold() {
        List<LineItem> items = new ArrayList<>();
        items.add(new LineItem("Widget", 1, new BigDecimal("100.00")));
        
        Receipt receipt = new OrderProcessor().processOrder(items);
        assert receipt.getSubtotal().equals(new BigDecimal("100.00"));
        assert receipt.getDiscount().equals(BigDecimal.ZERO);
        assert receipt.getVat().equals(new BigDecimal("21.00"));
        assert receipt.getTotal().equals(new BigDecimal("121.00"));
    }
    
    void testEmptyOrderThrowsException() {
        try {
            new OrderProcessor().processOrder(new ArrayList<>());
            assert false : "Should throw exception";
        } catch (IllegalArgumentException expected) {
        }
    }
    
    void testInvalidItemThrowsException() {
        List<LineItem> items = new ArrayList<>();
        items.add(new LineItem("", 1, new BigDecimal("50.00")));
        
        try {
            new OrderProcessor().processOrder(items);
            assert false : "Should throw exception";
        } catch (IllegalArgumentException expected) {
        }
    }
    
    void testReceiptFormatting() {
        List<LineItem> items = new ArrayList<>();
        items.add(new LineItem("Widget", 2, new BigDecimal("60.00")));
        
        Receipt receipt = new OrderProcessor().processOrder(items);
        String formatted = receipt.format();
        
        assert formatted.contains("Subtotal: €120.00");
        assert formatted.contains("Discount: -€12.00");
        assert formatted.contains("VAT (21%)");
        assert formatted.contains("Total: €130.68");
    }
    
    public static void main(String[] args) {
        OrderProcessorTest test = new OrderProcessorTest();
        test.testOrderUnder100NoDiscount();
        test.testOrderOver100WithDiscount();
        test.testMultipleLineItems();
        test.testOrderAtThreshold();
        test.testEmptyOrderThrowsException();
        test.testInvalidItemThrowsException();
        test.testReceiptFormatting();
        System.out.println("All tests passed!");
    }
}
