I'll write a complete order processing solution with line item validation, VAT calculation, conditional discount, and receipt generation.

**Assumptions:**
- Line items have description, quantity, and unit price
- Validation checks for non-negative prices and positive quantities
- 10% discount applies to pre-VAT subtotal (before VAT), then VAT is calculated on the discounted amount
- Receipt shows item breakdown, subtotal, discount (if applied), VAT, and final total

```java
class LineItem {
    private final String description;
    private final int quantity;
    private final double unitPrice;

    public LineItem(String description, int quantity, double unitPrice) {
        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public String getDescription() {
        return description;
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
```

```java
class Order {
    private static final double VAT_RATE = 0.21;
    private static final double DISCOUNT_RATE = 0.10;
    private static final double DISCOUNT_THRESHOLD = 100.0;

    private final java.util.List<LineItem> items = new java.util.ArrayList<>();

    public void addItem(LineItem item) {
        items.add(item);
    }

    public void validate() throws IllegalArgumentException {
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }
        for (LineItem item : items) {
            if (item.getQuantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be positive for: " + item.getDescription());
            }
            if (item.getUnitPrice() < 0) {
                throw new IllegalArgumentException("Unit price cannot be negative for: " + item.getDescription());
            }
        }
    }

    public String generateReceipt() {
        validate();

        double subtotal = items.stream().mapToDouble(LineItem::getLineTotal).sum();
        double discountAmount = subtotal > DISCOUNT_THRESHOLD ? subtotal * DISCOUNT_RATE : 0.0;
        double subtotalAfterDiscount = subtotal - discountAmount;
        double vat = subtotalAfterDiscount * VAT_RATE;
        double total = subtotalAfterDiscount + vat;

        StringBuilder receipt = new StringBuilder();
        receipt.append("========== RECEIPT ==========\n");
        for (LineItem item : items) {
            receipt.append(String.format("%-25s %5d x EUR %7.2f = EUR %8.2f\n",
                    item.getDescription(), item.getQuantity(), item.getUnitPrice(), item.getLineTotal()));
        }
        receipt.append("-----------------------------\n");
        receipt.append(String.format("%-40s EUR %8.2f\n", "Subtotal:", subtotal));
        if (discountAmount > 0) {
            receipt.append(String.format("%-40s EUR %8.2f\n", "Discount (10%):", discountAmount));
        }
        receipt.append(String.format("%-40s EUR %8.2f\n", "Subtotal after discount:", subtotalAfterDiscount));
        receipt.append(String.format("%-40s EUR %8.2f\n", "VAT (21%):", vat));
        receipt.append("=============================\n");
        receipt.append(String.format("%-40s EUR %8.2f\n", "TOTAL:", total));

        return receipt.toString();
    }
}
```

```java
public class Main {
    public static void main(String[] args) {
        Order order = new Order();
        order.addItem(new LineItem("Widget A", 5, 15.50));
        order.addItem(new LineItem("Widget B", 3, 20.00));
        order.addItem(new LineItem("Service", 1, 45.00));

        System.out.println(order.generateReceipt());
    }
}
```

**Example output for the sample order (pre-VAT total = 187.50 EUR, qualifies for 10% discount):**
```
========== RECEIPT ==========
Widget A                  5 x EUR   15.50 = EUR    77.50
Widget B                  3 x EUR   20.00 = EUR    60.00
Service                   1 x EUR   45.00 = EUR    45.00
-----------------------------
Subtotal:                                    EUR   182.50
Discount (10%):                              EUR    18.25
Subtotal after discount:                     EUR   164.25
VAT (21%):                                   EUR    34.49
=============================
TOTAL:                                       EUR   198.74
```