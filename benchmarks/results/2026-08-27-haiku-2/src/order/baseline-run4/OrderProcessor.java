public class OrderProcessor {
    
    static class OrderItem {
        String productName;
        int quantity;
        double unitPrice;
        
        OrderItem(String productName, int quantity, double unitPrice) {
            this.productName = productName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }
        
        double getLineTotal() {
            return quantity * unitPrice;
        }
    }
    
    static class Order {
        private final java.util.List<OrderItem> items = new java.util.ArrayList<>();
        private static final double VAT_RATE = 0.21;
        private static final double DISCOUNT_RATE = 0.10;
        private static final double DISCOUNT_THRESHOLD = 100.0;
        
        void addItem(OrderItem item) {
            validateItem(item);
            items.add(item);
        }
        
        private void validateItem(OrderItem item) {
            if (item.quantity <= 0) {
                throw new IllegalArgumentException("Quantity must be positive: " + item.quantity);
            }
            if (item.unitPrice < 0) {
                throw new IllegalArgumentException("Unit price cannot be negative: " + item.unitPrice);
            }
            if (item.productName == null || item.productName.trim().isEmpty()) {
                throw new IllegalArgumentException("Product name cannot be empty");
            }
        }
        
        double getSubtotal() {
            return items.stream().mapToDouble(OrderItem::getLineTotal).sum();
        }
        
        double getDiscount() {
            double subtotal = getSubtotal();
            return subtotal > DISCOUNT_THRESHOLD ? subtotal * DISCOUNT_RATE : 0.0;
        }
        
        double getDiscountedSubtotal() {
            return getSubtotal() - getDiscount();
        }
        
        double getVAT() {
            return getDiscountedSubtotal() * VAT_RATE;
        }
        
        double getTotal() {
            return getDiscountedSubtotal() + getVAT();
        }
        
        String getReceipt() {
            StringBuilder receipt = new StringBuilder();
            receipt.append("=== ORDER RECEIPT ===\n");
            receipt.append(String.format("%-30s %10s %10s %12s\n", "Product", "Qty", "Unit Price", "Line Total"));
            receipt.append("-".repeat(62)).append("\n");
            
            for (OrderItem item : items) {
                receipt.append(String.format("%-30s %10d €%9.2f €%11.2f\n", 
                    item.productName, 
                    item.quantity, 
                    item.unitPrice, 
                    item.getLineTotal()));
            }
            
            receipt.append("-".repeat(62)).append("\n");
            receipt.append(String.format("%-52s €%11.2f\n", "Subtotal:", getSubtotal()));
            
            double discount = getDiscount();
            if (discount > 0) {
                receipt.append(String.format("%-52s €%11.2f\n", "Discount (10%):", -discount));
            }
            
            receipt.append(String.format("%-52s €%11.2f\n", "VAT (21%):", getVAT()));
            receipt.append("=".repeat(62)).append("\n");
            receipt.append(String.format("%-52s €%11.2f\n", "TOTAL:", getTotal()));
            
            return receipt.toString();
        }
    }
    
    public static void main(String[] args) {
        Order order = new Order();
        order.addItem(new OrderItem("Laptop", 1, 65.00));
        order.addItem(new OrderItem("Mouse", 2, 15.00));
        order.addItem(new OrderItem("Keyboard", 1, 45.00));
        
        System.out.println(order.getReceipt());
        System.out.println("Final Total: €" + String.format("%.2f", order.getTotal()));
    }
}
