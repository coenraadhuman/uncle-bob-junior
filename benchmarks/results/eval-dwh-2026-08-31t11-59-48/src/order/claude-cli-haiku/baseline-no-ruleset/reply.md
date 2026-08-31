I'll write a complete Java solution that processes orders with validation, discount logic, and receipt generation.

**Assumptions:**
- Line items have description, quantity, and unit price
- Validation checks for non-null items, positive quantities, and non-negative prices
- 10% discount applies to the pre-VAT total when it exceeds €100
- VAT (21%) is applied after discount
- Receipt includes itemized list and summary

```java
import java.util.ArrayList;
import java.util.List;

public class OrderProcessor {
    
    static class LineItem {
        String description;
        int quantity;
        double unitPrice;
        
        public LineItem(String description, int quantity, double unitPrice) {
            this.description = description;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }
        
        double getTotal() {
            return quantity * unitPrice;
        }
    }
    
    static class Order {
        List<LineItem> lineItems;
        
        public Order() {
            this.lineItems = new ArrayList<>();
        }
        
        void addLineItem(LineItem item) {
            lineItems.add(item);
        }
        
        void validate() throws IllegalArgumentException {
            if (lineItems == null || lineItems.isEmpty()) {
                throw new IllegalArgumentException("Order must contain at least one line item");
            }
            for (LineItem item : lineItems) {
                if (item == null) {
                    throw new IllegalArgumentException("Line item cannot be null");
                }
                if (item.quantity <= 0) {
                    throw new IllegalArgumentException("Quantity must be positive: " + item.description);
                }
                if (item.unitPrice < 0) {
                    throw new IllegalArgumentException("Unit price cannot be negative: " + item.description);
                }
            }
        }
        
        String generateReceipt() {
            validate();
            
            double preVatTotal = 0;
            for (LineItem item : lineItems) {
                preVatTotal += item.getTotal();
            }
            
            double discount = 0;
            if (preVatTotal > 100) {
                discount = preVatTotal * 0.10;
            }
            
            double subtotalAfterDiscount = preVatTotal - discount;
            double vat = subtotalAfterDiscount * 0.21;
            double total = subtotalAfterDiscount + vat;
            
            StringBuilder receipt = new StringBuilder();
            receipt.append("=== RECEIPT ===\n");
            for (LineItem item : lineItems) {
                receipt.append(String.format("%s x%d @ €%.2f = €%.2f\n", 
                    item.description, item.quantity, item.unitPrice, item.getTotal()));
            }
            receipt.append("------------------------------\n");
            receipt.append(String.format("Subtotal:        €%.2f\n", preVatTotal));
            if (discount > 0) {
                receipt.append(String.format("Discount (10%%):  -€%.2f\n", discount));
            }
            receipt.append(String.format("After Discount:  €%.2f\n", subtotalAfterDiscount));
            receipt.append(String.format("VAT (21%%):       €%.2f\n", vat));
            receipt.append("------------------------------\n");
            receipt.append(String.format("TOTAL:           €%.2f\n", total));
            
            return receipt.toString();
        }
    }
    
    public static void main(String[] args) {
        Order order = new Order();
        order.addLineItem(new LineItem("Widget A", 2, 30.00));
        order.addLineItem(new LineItem("Widget B", 3, 20.00));
        order.addLineItem(new LineItem("Widget C", 1, 25.00));
        
        System.out.println(order.generateReceipt());
    }
}
```

This solution validates line items, calculates the pre-VAT total (€145 in the example), applies a 10% discount (€14.50), then adds 21% VAT to produce a formatted receipt with itemized details.