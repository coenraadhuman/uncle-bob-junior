import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OrderProcessor {
    
    static class LineItem {
        private final String description;
        private final double price;
        private final int quantity;
        
        public LineItem(String description, double price, int quantity) {
            this.description = Objects.requireNonNull(description, "Description cannot be null");
            this.price = price;
            this.quantity = quantity;
        }
        
        public String getDescription() {
            return description;
        }
        
        public double getPrice() {
            return price;
        }
        
        public int getQuantity() {
            return quantity;
        }
        
        public double getTotal() {
            return price * quantity;
        }
    }
    
    static class Order {
        private final List<LineItem> lineItems;
        private static final double VAT_RATE = 0.21;
        private static final double DISCOUNT_RATE = 0.10;
        private static final double DISCOUNT_THRESHOLD = 100.0;
        
        public Order(List<LineItem> lineItems) {
            this.lineItems = new ArrayList<>(Objects.requireNonNull(lineItems, "Line items cannot be null"));
        }
        
        public void validate() throws IllegalArgumentException {
            if (lineItems.isEmpty()) {
                throw new IllegalArgumentException("Order must contain at least one line item");
            }
            
            for (LineItem item : lineItems) {
                if (item.getPrice() < 0) {
                    throw new IllegalArgumentException("Price cannot be negative: " + item.getDescription());
                }
                if (item.getQuantity() <= 0) {
                    throw new IllegalArgumentException("Quantity must be positive: " + item.getDescription());
                }
            }
        }
        
        public double getSubtotal() {
            return lineItems.stream().mapToDouble(LineItem::getTotal).sum();
        }
        
        public double getDiscount() {
            double subtotal = getSubtotal();
            return subtotal > DISCOUNT_THRESHOLD ? subtotal * DISCOUNT_RATE : 0.0;
        }
        
        public double getSubtotalAfterDiscount() {
            return getSubtotal() - getDiscount();
        }
        
        public double getVat() {
            return getSubtotalAfterDiscount() * VAT_RATE;
        }
        
        public double getTotal() {
            return getSubtotalAfterDiscount() + getVat();
        }
        
        public String generateReceipt() {
            validate();
            StringBuilder receipt = new StringBuilder();
            receipt.append("╔════════════════════════════════════╗\n");
            receipt.append("║           ORDER RECEIPT            ║\n");
            receipt.append("╠════════════════════════════════════╣\n");
            
            for (LineItem item : lineItems) {
                receipt.append(String.format("║ %-25s %2d x €%6.2f║\n", 
                    truncate(item.getDescription(), 25), 
                    item.getQuantity(), 
                    item.getPrice()));
            }
            
            receipt.append("╠════════════════════════════════════╣\n");
            receipt.append(String.format("║ Subtotal:              €%10.2f ║\n", getSubtotal()));
            
            if (getDiscount() > 0) {
                receipt.append(String.format("║ Discount (10%):       -€%10.2f ║\n", getDiscount()));
            }
            
            receipt.append(String.format("║ VAT (21%):             €%10.2f ║\n", getVat()));
            receipt.append("╠════════════════════════════════════╣\n");
            receipt.append(String.format("║ TOTAL:                 €%10.2f ║\n", getTotal()));
            receipt.append("╚════════════════════════════════════╝\n");
            
            return receipt.toString();
        }
        
        private String truncate(String str, int length) {
            return str.length() > length ? str.substring(0, length - 2) + ".." : str;
        }
    }
    
    public static void main(String[] args) {
        List<LineItem> items = new ArrayList<>();
        items.add(new LineItem("Coffee", 4.50, 2));
        items.add(new LineItem("Sandwich", 8.75, 3));
        items.add(new LineItem("Cookie", 2.25, 5));
        
        Order order = new Order(items);
        System.out.println(order.generateReceipt());
    }
}
