I'll create Java code to process an order with validation, VAT calculation, conditional discount, and receipt generation.

**Assumptions:**
- Line items have description, quantity, and unit price
- Discount (10%) applies to pre-VAT subtotal, VAT (21%) calculated on discounted amount
- Validation checks for positive quantities and non-negative prices
- Receipt uses EUR currency format

```java
import java.util.ArrayList;
import java.util.List;

public class Order {
    private static final double VAT_RATE = 0.21;
    private static final double DISCOUNT_RATE = 0.10;
    private static final double DISCOUNT_THRESHOLD = 100.00;

    private List<LineItem> lineItems;

    public Order() {
        this.lineItems = new ArrayList<>();
    }

    public void addLineItem(String description, int quantity, double unitPrice) {
        lineItems.add(new LineItem(description, quantity, unitPrice));
    }

    public String processOrder() {
        validateLineItems();
        
        double preVatTotal = calculatePreVatTotal();
        double discount = preVatTotal > DISCOUNT_THRESHOLD ? preVatTotal * DISCOUNT_RATE : 0;
        double discountedTotal = preVatTotal - discount;
        double vat = discountedTotal * VAT_RATE;
        double finalTotal = discountedTotal + vat;

        return generateReceipt(preVatTotal, discount, discountedTotal, vat, finalTotal);
    }

    private void validateLineItems() {
        if (lineItems.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one line item");
        }
        for (LineItem item : lineItems) {
            if (item.quantity <= 0) {
                throw new IllegalArgumentException("Quantity must be positive: " + item.description);
            }
            if (item.unitPrice < 0) {
                throw new IllegalArgumentException("Price cannot be negative: " + item.description);
            }
        }
    }

    private double calculatePreVatTotal() {
        return lineItems.stream()
                .mapToDouble(item -> item.quantity * item.unitPrice)
                .sum();
    }

    private String generateReceipt(double preVatTotal, double discount, double discountedTotal,
                                   double vat, double finalTotal) {
        StringBuilder receipt = new StringBuilder();
        receipt.append("=== RECEIPT ===\n");
        
        for (LineItem item : lineItems) {
            double itemTotal = item.quantity * item.unitPrice;
            receipt.append(String.format("%s x%d @ EUR %.2f = EUR %.2f\n",
                    item.description, item.quantity, item.unitPrice, itemTotal));
        }
        
        receipt.append(String.format("\nSubtotal:         EUR %.2f\n", preVatTotal));
        if (discount > 0) {
            receipt.append(String.format("Discount (10%%):  -EUR %.2f\n", discount));
        }
        receipt.append(String.format("After discount:   EUR %.2f\n", discountedTotal));
        receipt.append(String.format("VAT (21%%):        EUR %.2f\n", vat));
        receipt.append(String.format("TOTAL:            EUR %.2f\n", finalTotal));
        
        return receipt.toString();
    }

    private static class LineItem {
        String description;
        int quantity;
        double unitPrice;

        LineItem(String description, int quantity, double unitPrice) {
            this.description = description;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }
    }

    public static void main(String[] args) {
        Order order = new Order();
        order.addLineItem("Widget A", 5, 15.00);
        order.addLineItem("Widget B", 3, 20.00);
        
        System.out.println(order.processOrder());
    }
}
```

The code validates all line items, calculates the pre-VAT total, applies a 10% discount if it exceeds 100 EUR, computes 21% VAT on the discounted amount, and formats a detailed receipt with itemised breakdown.