I'll write a complete Java solution for order processing with validation, discount logic, VAT calculation, and receipt generation.

**Assumptions:**
- Line items have a product name, unit price (EUR), and quantity
- Validation ensures positive prices/quantities and non-empty items
- Discount (10%) applies to subtotal when pre-VAT total exceeds €100
- VAT (21%) is calculated on the discounted subtotal
- Receipt is formatted as a readable string with itemized breakdown

```java
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

class LineItem {
    private String productName;
    private double unitPrice;
    private int quantity;
    
    public LineItem(String productName, double unitPrice, int quantity) {
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }
    
    public double getLineTotal() {
        return unitPrice * quantity;
    }
    
    public String getProductName() {
        return productName;
    }
    
    public double getUnitPrice() {
        return unitPrice;
    }
    
    public int getQuantity() {
        return quantity;
    }
}
```

**Example usage:**

```java
public class Main {
    public static void main(String[] args) {
        Order order = new Order();
        order.addItem("Coffee", 5.50, 2);
        order.addItem("Sandwich", 8.75, 3);
        order.addItem("Juice", 3.25, 4);
        
        System.out.println(order.generateReceipt());
    }
}
```

**Output:**
```
====== ORDER RECEIPT ======
Coffee x 2 @ €5.50 = €11.00
Sandwich x 3 @ €8.75 = €26.25
Juice x 4 @ €3.25 = €13.00
---------------------------
Subtotal:        €50.25
Subtotal (VAT):  €50.25
VAT (21%):       €10.55
---------------------------
TOTAL:           €60.80
===========================
```

(When subtotal exceeds €100, the 10% discount line appears and VAT is calculated on the discounted amount.)