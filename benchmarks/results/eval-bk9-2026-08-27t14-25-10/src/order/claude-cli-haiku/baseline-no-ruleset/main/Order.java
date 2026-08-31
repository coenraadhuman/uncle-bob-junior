import java.util.ArrayList;
import java.util.List;

public class Order {
    private List<LineItem> items;
    
    public Order() {
        this.items = new ArrayList<>();
    }
    
    public void addItem(String productName, double unitPrice, int quantity) {
        if (productName == null || productName.isBlank()) {
            throw new IllegalArgumentException("Product name cannot be empty");
        }
        if (unitPrice <= 0) {
            throw new IllegalArgumentException("Unit price must be positive");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        items.add(new LineItem(productName, unitPrice, quantity));
    }
    
    public String generateReceipt() {
        validateOrder();
        
        double subtotal = calculateSubtotal();
        double discount = subtotal > 100 ? subtotal * 0.10 : 0;
        double discountedSubtotal = subtotal - discount;
        double vat = discountedSubtotal * 0.21;
        double total = discountedSubtotal + vat;
        
        StringBuilder receipt = new StringBuilder();
        receipt.append("====== ORDER RECEIPT ======\n");
        
        for (LineItem item : items) {
            receipt.append(String.format("%s x %d @ €%.2f = €%.2f\n",
                item.getProductName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getLineTotal()));
        }
        
        receipt.append("---------------------------\n");
        receipt.append(String.format("Subtotal:        €%.2f\n", subtotal));
        
        if (discount > 0) {
            receipt.append(String.format("Discount (10%%):  -€%.2f\n", discount));
        }
        
        receipt.append(String.format("Subtotal (VAT):  €%.2f\n", discountedSubtotal));
        receipt.append(String.format("VAT (21%%):       €%.2f\n", vat));
        receipt.append("---------------------------\n");
        receipt.append(String.format("TOTAL:           €%.2f\n", total));
        receipt.append("===========================\n");
        
        return receipt.toString();
    }
    
    private void validateOrder() {
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }
    }
    
    private double calculateSubtotal() {
        return items.stream().mapToDouble(LineItem::getLineTotal).sum();
    }
}
