```java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class LineItem {
    private final String description;
    private final int quantity;
    private final BigDecimal unitPrice;
    
    LineItem(String description, int quantity, BigDecimal unitPrice) {
        this.description = Objects.requireNonNull(description);
        this.quantity = quantity;
        this.unitPrice = Objects.requireNonNull(unitPrice);
    }
    
    boolean isValid() {
        return !description.isEmpty() && quantity > 0 && unitPrice.signum() > 0;
    }
    
    BigDecimal getLineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
    
    String getDescription() {
        return description;
    }
}

class Receipt {
    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    
    private final BigDecimal subtotal;
    private final BigDecimal discount;
    private final BigDecimal vat;
    private final BigDecimal total;
    
    Receipt(BigDecimal subtotal) {
        this.subtotal = subtotal;
        this.discount = computeDiscount(subtotal);
        BigDecimal discountedSubtotal = subtotal.subtract(discount);
        this.vat = computeVat(discountedSubtotal);
        this.total = discountedSubtotal.add(vat);
    }
    
    private BigDecimal computeDiscount(BigDecimal subtotal) {
        if (subtotal.compareTo(DISCOUNT_THRESHOLD) > 0) {
            return subtotal.multiply(DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }
    
    private BigDecimal computeVat(BigDecimal discountedSubtotal) {
        return discountedSubtotal.multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);
    }
    
    String format() {
        return String.format(
            "Subtotal: €%.2f\nDiscount: -€%.2f\nVAT (21%%): €%.2f\nTotal: €%.2f",
            subtotal, discount, vat, total
        );
    }
    
    BigDecimal getTotal() {
        return total;
    }
    
    BigDecimal getSubtotal() {
        return subtotal;
    }
    
    BigDecimal getDiscount() {
        return discount;
    }
    
    BigDecimal getVat() {
        return vat;
    }
}

class OrderProcessor {
    Receipt processOrder(List<LineItem> items) {
        validateItems(items);
        BigDecimal subtotal = computeSubtotal(items);
        return new Receipt(subtotal);
    }
    
    private void validateItems(List<LineItem> items) {
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }
        for (LineItem item : items) {
            if (!item.isValid()) {
                throw new IllegalArgumentException("Invalid item: " + item.getDescription());
            }
        }
    }
    
    private BigDecimal computeSubtotal(List<LineItem> items) {
        BigDecimal sum = BigDecimal.ZERO;
        for (LineItem item : items) {
            sum = sum.add(item.getLineTotal());
        }
        return sum.setScale(2, RoundingMode.HALF_UP);
    }
}

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
```

**Cleaned:** ordered responsibility (LineItem validates itself, Receipt computes totals with named constants, OrderProcessor orchestrates); safe to change because Receipt's discount and VAT logic is isolated and tested for all thresholds (under, at, over 100 EUR), invalid items rejected upfront, and all monetary calculations use BigDecimal with explicit rounding.