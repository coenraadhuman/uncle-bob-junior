import java.util.ArrayList;
import java.util.List;

public class OrderProcessor {
    
    static class LineItem {
        String product;
        int quantity;
        double unitPrice;
        
        public LineItem(String product, int quantity, double unitPrice) {
            this.product = product;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }
        
        public double getTotal() {
            return quantity * unitPrice;
        }
    }
    
    static class Order {
        private List<LineItem> items;
        private static final double VAT_RATE = 0.21;
        private static final double DISCOUNT_RATE = 0.10;
        private static final double DISCOUNT_THRESHOLD = 100.0;
        
        public Order(List<LineItem> items) {
            this.items = items;
        }
        
        // Validate all line items
        public void validate() {
            for (LineItem item : items) {
                if (item.quantity <= 0 || item.unitPrice < 0) {
                    throw new IllegalArgumentException(
                        "Invalid item: " + item.product + " (qty: " + item.quantity + 
                        ", price: €" + item.unitPrice + ")");
                }
            }
        }
        
        // Calculate subtotal before VAT
        private double calculateSubtotal() {
            double sum = 0;
            for (LineItem item : items) {
                sum += item.getTotal();
            }
            return sum;
        }
        
        // Generate receipt with totals
        public String generateReceipt() {
            validate();
            
            StringBuilder receipt = new StringBuilder();
            receipt.append("=== RECEIPT ===\n");
            
            // Itemize products
            for (LineItem item : items) {
                double lineTotal = item.getTotal();
                receipt.append(String.format("%s x%d @ €%.2f = €%.2f\n", 
                    item.product, item.quantity, item.unitPrice, lineTotal));
            }
            
            // Calculate amounts
            double subtotal = calculateSubtotal();
            double discount = subtotal > DISCOUNT_THRESHOLD ? subtotal * DISCOUNT_RATE : 0.0;
            double subtotalAfterDiscount = subtotal - discount;
            double vat = subtotalAfterDiscount * VAT_RATE;
            double total = subtotalAfterDiscount + vat;
            
            receipt.append("---\n");
            receipt.append(String.format("Subtotal:    €%.2f\n", subtotal));
            
            if (discount > 0) {
                receipt.append(String.format("Discount:    -€%.2f\n", discount));
            }
            
            receipt.append(String.format("Subtotal:    €%.2f\n", subtotalAfterDiscount));
            receipt.append(String.format("VAT (21%%):   €%.2f\n", vat));
            receipt.append(String.format("TOTAL:       €%.2f\n", total));
            
            return receipt.toString();
        }
    }
    
    // Example usage
    public static void main(String[] args) {
        List<LineItem> items = new ArrayList<>();
        items.add(new LineItem("Laptop", 1, 75.00));
        items.add(new LineItem("Mouse", 2, 15.00));
        items.add(new LineItem("Keyboard", 1, 25.00));
        
        Order order = new Order(items);
        System.out.println(order.generateReceipt());
    }
}
