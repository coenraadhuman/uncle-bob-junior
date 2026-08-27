import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final int DECIMAL_PLACES = 2;
    
    private final List<LineItem> lineItems;
    
    public Order(List<LineItem> lineItems) {
        if (lineItems == null || lineItems.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one line item");
        }
        this.lineItems = new ArrayList<>(lineItems);
    }
    
    public String generateReceipt() {
        BigDecimal subtotal = calculateSubtotal();
        BigDecimal discountAmount = calculateDiscount(subtotal);
        BigDecimal discountedSubtotal = subtotal.subtract(discountAmount);
        BigDecimal vatAmount = calculateVat(discountedSubtotal);
        BigDecimal total = discountedSubtotal.add(vatAmount);
        
        return formatReceipt(subtotal, discountAmount, discountedSubtotal, vatAmount, total);
    }
    
    private BigDecimal calculateSubtotal() {
        return lineItems.stream()
            .map(LineItem::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    private BigDecimal calculateDiscount(BigDecimal subtotal) {
        if (subtotal.compareTo(DISCOUNT_THRESHOLD) > 0) {
            return subtotal.multiply(DISCOUNT_RATE)
                .setScale(DECIMAL_PLACES, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }
    
    private BigDecimal calculateVat(BigDecimal amount) {
        return amount.multiply(VAT_RATE)
            .setScale(DECIMAL_PLACES, RoundingMode.HALF_UP);
    }
    
    private String formatReceipt(BigDecimal subtotal, BigDecimal discount,
                                 BigDecimal discountedSubtotal, BigDecimal vat,
                                 BigDecimal total) {
        StringBuilder receipt = new StringBuilder();
        receipt.append("=== ORDER RECEIPT ===\n");
        receipt.append(formatLineItems());
        receipt.append(String.format("\nSubtotal: €%.2f\n", subtotal));
        
        if (discount.compareTo(BigDecimal.ZERO) > 0) {
            receipt.append(String.format("Discount (10%%): -€%.2f\n", discount));
        }
        
        receipt.append(String.format("Subtotal after discount: €%.2f\n", discountedSubtotal));
        receipt.append(String.format("VAT (21%%): €%.2f\n", vat));
        receipt.append(String.format("\nTOTAL: €%.2f\n", total));
        receipt.append("====================");
        
        return receipt.toString();
    }
    
    private String formatLineItems() {
        StringBuilder items = new StringBuilder();
        for (LineItem item : lineItems) {
            items.append(String.format("  %s x%d @ €%.2f = €%.2f\n",
                item.getDescription(),
                item.getQuantity(),
                item.getPrice(),
                item.getSubtotal()));
        }
        return items.toString();
    }
}
