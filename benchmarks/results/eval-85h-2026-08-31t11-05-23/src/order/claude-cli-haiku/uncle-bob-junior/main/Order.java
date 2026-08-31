import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

class Order {
    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100");
    private static final int CURRENCY_SCALE = 2;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;
    
    private final List<LineItem> items;
    
    Order(List<LineItem> items) {
        this.items = List.copyOf(items);
    }
    
    void validate() throws IllegalArgumentException {
        for (LineItem item : items) {
            if (item.quantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be positive");
            }
            if (item.unitPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Unit price cannot be negative");
            }
        }
    }
    
    BigDecimal subtotal() {
        return items.stream()
            .map(LineItem::subtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    BigDecimal discountAmount() {
        BigDecimal preVatTotal = subtotal();
        if (preVatTotal.compareTo(DISCOUNT_THRESHOLD) > 0) {
            return preVatTotal.multiply(DISCOUNT_RATE).setScale(CURRENCY_SCALE, ROUNDING);
        }
        return BigDecimal.ZERO;
    }
    
    BigDecimal totalBeforeVat() {
        return subtotal().subtract(discountAmount());
    }
    
    BigDecimal vat() {
        return totalBeforeVat().multiply(VAT_RATE).setScale(CURRENCY_SCALE, ROUNDING);
    }
    
    BigDecimal total() {
        return totalBeforeVat().add(vat()).setScale(CURRENCY_SCALE, ROUNDING);
    }
    
    String receipt() {
        validate();
        
        StringBuilder receipt = new StringBuilder();
        receipt.append("=== RECEIPT ===\n");
        receipt.append(formatLineItems());
        receipt.append(formatSummary());
        
        return receipt.toString();
    }
    
    private String formatLineItems() {
        StringBuilder sb = new StringBuilder();
        for (LineItem item : items) {
            BigDecimal itemTotal = item.subtotal();
            sb.append(String.format("%-20s x%3d @ €%8.2f = €%8.2f\n",
                item.description(),
                item.quantity(),
                item.unitPrice().doubleValue(),
                itemTotal.doubleValue()));
        }
        return sb.toString();
    }
    
    private String formatSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append(String.format("Subtotal:                          €%8.2f\n", subtotal().doubleValue()));
        
        BigDecimal discount = discountAmount();
        if (discount.compareTo(BigDecimal.ZERO) > 0) {
            sb.append(String.format("Discount (10%%):                   -€%8.2f\n", discount.doubleValue()));
        }
        
        sb.append(String.format("Subtotal after discount:           €%8.2f\n", totalBeforeVat().doubleValue()));
        sb.append(String.format("VAT (21%%):                         €%8.2f\n", vat().doubleValue()));
        sb.append(String.format("Total:                             €%8.2f\n", total().doubleValue()));
        
        return sb.toString();
    }
}
