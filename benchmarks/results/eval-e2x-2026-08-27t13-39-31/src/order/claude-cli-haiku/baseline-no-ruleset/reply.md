I'll create a complete Java solution for order processing with validation, VAT calculation, and discount logic.

**Assumptions:**
- Line items have: product name, quantity, unit price
- Validation: quantity > 0, unit price ≥ 0
- Discount (10%) applies to pre-VAT subtotal when it exceeds €100
- Receipt shows itemized list, subtotal, discount (if applied), VAT (21%), and total
- All amounts in euros

```java
import java.util.ArrayList;
import java.util.List;

public class LineItem {
    private String product;
    private int quantity;
    private double unitPrice;
    
    public LineItem(String product, int quantity, double unitPrice) {
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }
    
    public boolean isValid() {
        return quantity > 0 && unitPrice >= 0;
    }
    
    public double getTotal() {
        return quantity * unitPrice;
    }
    
    public String getProduct() {
        return product;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public double getUnitPrice() {
        return unitPrice;
    }
}
```

```java
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
```

```java
public class OrderProcessor {
    public static void main(String[] args) {
        Order order = new Order();
        
        order.addItem(new LineItem("Laptop", 1, 65.50));
        order.addItem(new LineItem("Mouse", 2, 12.75));
        order.addItem(new LineItem("Keyboard", 1, 35.00));
        
        System.out.println(order.generateReceipt());
    }
}
```

**Example output** (with the sample items totaling €125.00 pre-VAT):
```
============ RECEIPT ============
Laptop x1 @ €65.50 = €65.50
Mouse x2 @ €12.75 = €25.50
Keyboard x1 @ €35.00 = €35.00
--------------------------------
Subtotal:              €126.00
Discount (10%):       -€12.60
After discount:        €113.40
VAT (21%):             €23.81
================================
TOTAL:                 €137.21
================================
```