I'll provide a complete Java solution for order processing with receipt generation.

**Assumptions:**
- Line items have description, quantity, and unit price
- Validation checks for positive quantity and non-negative price
- Discount applies to pre-VAT subtotal when exceeding €100
- VAT (21%) is calculated on the discounted amount
- Quantities are integers, prices are doubles in euros

```java
import java.util.*;
import java.text.DecimalFormat;

class OrderProcessor {
    
    static class LineItem {
        String description;
        int quantity;
        double unitPrice;
        
        LineItem(String description, int quantity, double unitPrice) {
            this.description = description;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }
    }
    
    static void validateLineItems(List<LineItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }
        for (LineItem item : items) {
            if (item.quantity <= 0) {
                throw new IllegalArgumentException("Quantity must be positive");
            }
            if (item.unitPrice < 0) {
                throw new IllegalArgumentException("Price cannot be negative");
            }
        }
    }
    
    static String processOrder(List<LineItem> items) {
        validateLineItems(items);
        
        double preVatTotal = 0;
        for (LineItem item : items) {
            preVatTotal += item.quantity * item.unitPrice;
        }
        
        double discount = preVatTotal > 100 ? preVatTotal * 0.10 : 0;
        double postDiscountTotal = preVatTotal - discount;
        double vat = postDiscountTotal * 0.21;
        double finalTotal = postDiscountTotal + vat;
        
        DecimalFormat fmt = new DecimalFormat("0.00");
        StringBuilder receipt = new StringBuilder();
        
        receipt.append("========== RECEIPT ==========\n");
        for (LineItem item : items) {
            double lineTotal = item.quantity * item.unitPrice;
            receipt.append(String.format("%s (qty: %d @ €%s) = €%s\n",
                item.description, item.quantity, fmt.format(item.unitPrice), fmt.format(lineTotal)));
        }
        receipt.append("-----------------------------\n");
        receipt.append(String.format("Subtotal:        €%s\n", fmt.format(preVatTotal)));
        if (discount > 0) {
            receipt.append(String.format("Discount (10%%):  -€%s\n", fmt.format(discount)));
        }
        receipt.append(String.format("VAT (21%%):       €%s\n", fmt.format(vat)));
        receipt.append("=============================\n");
        receipt.append(String.format("TOTAL:           €%s\n", fmt.format(finalTotal)));
        
        return receipt.toString();
    }
    
    public static void main(String[] args) {
        List<LineItem> order = Arrays.asList(
            new LineItem("Widget A", 3, 25.00),
            new LineItem("Widget B", 2, 40.00),
            new LineItem("Service", 1, 20.00)
        );
        
        System.out.println(processOrder(order));
    }
}
```

The code validates items, calculates the pre-VAT subtotal, applies a 10% discount if it exceeds €100, adds 21% VAT to the discounted amount, and outputs a formatted receipt string.