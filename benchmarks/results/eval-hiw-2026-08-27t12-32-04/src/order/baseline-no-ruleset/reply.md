I'll create a complete Java solution for order processing with line item validation, VAT calculation, conditional discount, and receipt generation.

**Assumptions:**
- Line items contain product name, quantity, and unit price
- Validation checks for positive quantities and prices
- 10% discount applies to pre-VAT total when it exceeds €100
- VAT (21%) is calculated on the discounted pre-VAT amount
- Receipt shows itemized breakdown with calculations

```java
import java.util.ArrayList;
import java.util.List;

class LineItem {
    private String productName;
    private int quantity;
    private double unitPrice;

    public LineItem(String productName, int quantity, double unitPrice) {
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public boolean isValid() {
        return quantity > 0 && unitPrice > 0;
    }

    public String getProductName() {
        return productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public double getLineTotal() {
        return quantity * unitPrice;
    }
}

class Order {
    private List<LineItem> lineItems;
    private static final double VAT_RATE = 0.21;
    private static final double DISCOUNT_RATE = 0.10;
    private static final double DISCOUNT_THRESHOLD = 100.0;

    public Order() {
        this.lineItems = new ArrayList<>();
    }

    public void addLineItem(LineItem item) {
        lineItems.add(item);
    }

    public boolean validateLineItems() {
        for (LineItem item : lineItems) {
            if (!item.isValid()) {
                return false;
            }
        }
        return lineItems.size() > 0;
    }

    public double getPreVatTotal() {
        double total = 0;
        for (LineItem item : lineItems) {
            total += item.getLineTotal();
        }
        return total;
    }

    public double getDiscountAmount() {
        double preVatTotal = getPreVatTotal();
        if (preVatTotal > DISCOUNT_THRESHOLD) {
            return preVatTotal * DISCOUNT_RATE;
        }
        return 0;
    }

    public double getDiscountedTotal() {
        return getPreVatTotal() - getDiscountAmount();
    }

    public double getVatAmount() {
        return getDiscountedTotal() * VAT_RATE;
    }

    public double getFinalTotal() {
        return getDiscountedTotal() + getVatAmount();
    }

    public String generateReceipt() {
        if (!validateLineItems()) {
            return "Error: Invalid line items";
        }

        StringBuilder receipt = new StringBuilder();
        receipt.append("=== ORDER RECEIPT ===\n\n");

        receipt.append("Line Items:\n");
        for (LineItem item : lineItems) {
            receipt.append(String.format("  %-30s %3d x €%8.2f = €%8.2f\n",
                    item.getProductName(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getLineTotal()));
        }

        receipt.append("\n----------------------------------------\n");
        receipt.append(String.format("Pre-VAT Total:              €%8.2f\n", getPreVatTotal()));

        double discount = getDiscountAmount();
        if (discount > 0) {
            receipt.append(String.format("Discount (10%):            -€%8.2f\n", discount));
            receipt.append(String.format("Discounted Total:           €%8.2f\n", getDiscountedTotal()));
        }

        receipt.append(String.format("VAT (21%):                  €%8.2f\n", getVatAmount()));
        receipt.append("----------------------------------------\n");
        receipt.append(String.format("FINAL TOTAL:                €%8.2f\n", getFinalTotal()));

        return receipt.toString();
    }
}

public class OrderProcessor {
    public static void main(String[] args) {
        Order order = new Order();

        // Example 1: Order with total > 100 (gets discount)
        order.addLineItem(new LineItem("Widget A", 5, 20.0));
        order.addLineItem(new LineItem("Widget B", 3, 10.0));
        order.addLineItem(new LineItem("Service", 1, 5.0));

        System.out.println(order.generateReceipt());
        System.out.println("\n\n");

        // Example 2: Order with total < 100 (no discount)
        Order order2 = new Order();
        order2.addLineItem(new LineItem("Item X", 2, 25.0));
        order2.addLineItem(new LineItem("Item Y", 1, 40.0));

        System.out.println(order2.generateReceipt());
    }
}
```

**Output example:**
```
=== ORDER RECEIPT ===

Line Items:
  Widget A                      5 x €   20.00 = €  100.00
  Widget B                      3 x €   10.00 = €   30.00
  Service                       1 x €    5.00 = €    5.00

----------------------------------------
Pre-VAT Total:              €  135.00
Discount (10%):            -€   13.50
Discounted Total:           €  121.50
VAT (21%):                  €   25.52
----------------------------------------
FINAL TOTAL:                €  147.02
```