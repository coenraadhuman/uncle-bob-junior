import java.util.ArrayList;
import java.util.List;

public class Order {
    private List<LineItem> items = new ArrayList<>();
    private static final double VAT_RATE = 0.21;
    private static final double DISCOUNT_RATE = 0.10;
    private static final double DISCOUNT_THRESHOLD = 100.00;
    
    public void addItem(LineItem item) {
        if (!item.isValid()) {
            throw new IllegalArgumentException(
                "Invalid line item: quantity must be > 0, unit price must be >= 0");
        }
        items.add(item);
    }
    
    public String generateReceipt() {
        if (items.isEmpty()) {
            throw new IllegalStateException("Cannot generate receipt for empty order");
        }
        
        double subtotal = calculateSubtotal();
        double discount = calculateDiscount(subtotal);
        double subtotalAfterDiscount = subtotal - discount;
        double vat = subtotalAfterDiscount * VAT_RATE;
        double total = subtotalAfterDiscount + vat;
        
        StringBuilder receipt = new StringBuilder();
        receipt.append("============ RECEIPT ============\n");
        
        for (LineItem item : items) {
            receipt.append(String.format("%s x%d @ €%.2f = €%.2f\n",
                item.getProduct(), item.getQuantity(), item.getUnitPrice(), item.getTotal()));
        }
        
        receipt.append("--------------------------------\n");
        receipt.append(String.format("Subtotal:              €%.2f\n", subtotal));
        
        if (discount > 0) {
            receipt.append(String.format("Discount (10%%):       -€%.2f\n", discount));
            receipt.append(String.format("After discount:        €%.2f\n", subtotalAfterDiscount));
        }
        
        receipt.append(String.format("VAT (21%%):             €%.2f\n", vat));
        receipt.append("================================\n");
        receipt.append(String.format("TOTAL:                 €%.2f\n", total));
        receipt.append("================================\n");
        
        return receipt.toString();
    }
    
    private double calculateSubtotal() {
        return items.stream().mapToDouble(LineItem::getTotal).sum();
    }
    
    private double calculateDiscount(double subtotal) {
        return subtotal > DISCOUNT_THRESHOLD ? subtotal * DISCOUNT_RATE : 0;
    }
}
