import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

class Order {
    private List<LineItem> items = new ArrayList<>();
    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    
    public void addLineItem(String description, BigDecimal price) {
        LineItem item = new LineItem(description, price);
        validateLineItem(item);
        items.add(item);
    }
    
    private void validateLineItem(LineItem item) {
        if (item.getDescription() == null || item.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Item description cannot be null or empty");
        }
        if (item.getPrice() == null || item.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Item price must be positive");
        }
    }
    
    public String generateReceipt() {
        BigDecimal subtotal = calculateSubtotal();
        BigDecimal discount = subtotal.compareTo(DISCOUNT_THRESHOLD) > 0 
            ? subtotal.multiply(DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
        BigDecimal afterDiscount = subtotal.subtract(discount);
        BigDecimal vat = afterDiscount.multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = afterDiscount.add(vat);
        
        StringBuilder receipt = new StringBuilder();
        receipt.append("=== ORDER RECEIPT ===\n");
        for (LineItem item : items) {
            receipt.append(String.format("%-35s € %7.2f\n", item.getDescription(), item.getPrice()));
        }
        receipt.append("-------------------------------------\n");
        receipt.append(String.format("%-35s € %7.2f\n", "Subtotal", subtotal));
        if (discount.compareTo(BigDecimal.ZERO) > 0) {
            receipt.append(String.format("%-35s € -%6.2f\n", "Discount (10%)", discount));
        }
        receipt.append(String.format("%-35s € %7.2f\n", "VAT (21%)", vat));
        receipt.append("-------------------------------------\n");
        receipt.append(String.format("%-35s € %7.2f\n", "TOTAL", total));
        
        return receipt.toString();
    }
    
    private BigDecimal calculateSubtotal() {
        return items.stream()
            .map(LineItem::getPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
