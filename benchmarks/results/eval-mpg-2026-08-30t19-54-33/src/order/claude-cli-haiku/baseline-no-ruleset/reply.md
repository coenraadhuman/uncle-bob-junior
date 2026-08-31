```java
import java.util.ArrayList;
import java.util.List;

class LineItem {
    String description;
    double price;
    int quantity;
    
    LineItem(String description, double price, int quantity) {
        this.description = description;
        this.price = price;
        this.quantity = quantity;
    }
    
    double getTotal() {
        return price * quantity;
    }
}

class Order {
    private List<LineItem> items;
    private static final double VAT_RATE = 0.21;
    private static final double DISCOUNT_RATE = 0.10;
    private static final double DISCOUNT_THRESHOLD = 100.0;
    
    Order(List<LineItem> items) {
        this.items = items;
    }
    
    boolean validate() {
        for (LineItem item : items) {
            if (item.price <= 0 || item.quantity <= 0) {
                return false;
            }
        }
        return true;
    }
    
    double getSubtotal() {
        double total = 0;
        for (LineItem item : items) {
            total += item.getTotal();
        }
        return total;
    }
    
    double getDiscount() {
        double subtotal = getSubtotal();
        if (subtotal > DISCOUNT_THRESHOLD) {
            return subtotal * DISCOUNT_RATE;
        }
        return 0;
    }
    
    double getVAT() {
        double discountedSubtotal = getSubtotal() - getDiscount();
        return discountedSubtotal * VAT_RATE;
    }
    
    double getTotal() {
        return getSubtotal() - getDiscount() + getVAT();
    }
    
    String getReceipt() {
        StringBuilder receipt = new StringBuilder();
        receipt.append("=== RECEIPT ===\n");
        
        for (LineItem item : items) {
            receipt.append(String.format("%s x%d @ EUR %.2f = EUR %.2f\n", 
                item.description, item.quantity, item.price, item.getTotal()));
        }
        
        receipt.append(String.format("\nSubtotal: EUR %.2f\n", getSubtotal()));
        
        double discount = getDiscount();
        if (discount > 0) {
            receipt.append(String.format("Discount (10%%): -EUR %.2f\n", discount));
        }
        
        receipt.append(String.format("VAT (21%%): EUR %.2f\n", getVAT()));
        receipt.append(String.format("Total: EUR %.2f\n", getTotal()));
        
        return receipt.toString();
    }
}

public class OrderProcessor {
    public static void main(String[] args) {
        List<LineItem> items = new ArrayList<>();
        items.add(new LineItem("Widget A", 25.00, 2));
        items.add(new LineItem("Widget B", 30.00, 2));
        items.add(new LineItem("Widget C", 15.00, 1));
        
        Order order = new Order(items);
        
        if (!order.validate()) {
            System.out.println("Invalid order items");
            return;
        }
        
        System.out.println(order.getReceipt());
    }
}
```

**Assumptions:**
- Line items have description, unit price, and quantity; validation rejects zero or negative prices/quantities
- Discount applies to pre-VAT subtotal when it exceeds 100 EUR (not >= 100)
- VAT applies to the discounted subtotal
- Receipt shows itemized list, subtotal, discount (if applied), VAT amount, and final total in EUR