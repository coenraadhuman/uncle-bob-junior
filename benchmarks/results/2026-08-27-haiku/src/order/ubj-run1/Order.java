import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class Order {
    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100");
    
    private final List<LineItem> items;
    
    public Order(List<LineItem> items) {
        validateItems(items);
        this.items = items;
    }
    
    private void validateItems(List<LineItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }
        items.forEach(LineItem::validate);
    }
    
    public String generateReceipt() {
        BigDecimal subtotal = calculateSubtotal();
        BigDecimal discount = calculateDiscount(subtotal);
        BigDecimal discountedSubtotal = subtotal.subtract(discount);
        BigDecimal vat = calculateVat(discountedSubtotal);
        BigDecimal total = discountedSubtotal.add(vat);
        
        return formatReceipt(subtotal, discount, discountedSubtotal, vat, total);
    }
    
    private BigDecimal calculateSubtotal() {
        return items.stream()
            .map(LineItem::getLineTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    private BigDecimal calculateDiscount(BigDecimal subtotal) {
        if (subtotal.compareTo(DISCOUNT_THRESHOLD) > 0) {
            return subtotal.multiply(DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }
    
    private BigDecimal calculateVat(BigDecimal amount) {
        return amount.multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);
    }
    
    private String formatReceipt(BigDecimal subtotal, BigDecimal discount, 
                                  BigDecimal discountedSubtotal, BigDecimal vat, 
                                  BigDecimal total) {
        StringBuilder receipt = new StringBuilder();
        receipt.append("=== RECEIPT ===\n");
        receipt.append("Items:\n");
        
        items.forEach(item -> 
            receipt.append(String.format("  %s x%d @ €%.2f = €%.2f\n", 
                item.getProductName(), 
                item.getQuantity(),
                item.getUnitPrice(),
                item.getLineTotal()))
        );
        
        receipt.append(String.format("\nSubtotal:  €%.2f\n", subtotal));
        
        if (discount.compareTo(BigDecimal.ZERO) > 0) {
            receipt.append(String.format("Discount (10%%) -€%.2f\n", discount));
        }
        
        receipt.append(String.format("Subtotal after discount: €%.2f\n", discountedSubtotal));
        receipt.append(String.format("VAT (21%%):  €%.2f\n", vat));
        receipt.append(String.format("TOTAL:  €%.2f\n", total));
        
        return receipt.toString();
    }
}
