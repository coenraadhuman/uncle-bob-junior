```java
import java.util.List;
import java.util.ArrayList;

class LineItem {
    private String description;
    private int quantity;
    private double unitPrice;

    public LineItem(String description, int quantity, double unitPrice) {
        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public double getTotal() {
        return quantity * unitPrice;
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
}

class OrderProcessor {
    private static final double VAT_RATE = 0.21;
    private static final double DISCOUNT_RATE = 0.10;
    private static final double DISCOUNT_THRESHOLD = 100.0;

    public static String processOrder(List<LineItem> lineItems) {
        validateLineItems(lineItems);

        double subtotal = lineItems.stream()
                .mapToDouble(LineItem::getTotal)
                .sum();

        double discount = subtotal > DISCOUNT_THRESHOLD ? subtotal * DISCOUNT_RATE : 0.0;
        double preVatTotal = subtotal - discount;
        double vat = preVatTotal * VAT_RATE;
        double total = preVatTotal + vat;

        return buildReceipt(lineItems, subtotal, discount, preVatTotal, vat, total);
    }

    private static void validateLineItems(List<LineItem> lineItems) {
        if (lineItems == null || lineItems.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one line item");
        }
        for (LineItem item : lineItems) {
            if (item.getDescription() == null || item.getDescription().trim().isEmpty()) {
                throw new IllegalArgumentException("Line item description cannot be empty");
            }
            if (item.getQuantity() <= 0) {
                throw new IllegalArgumentException("Line item quantity must be positive");
            }
            if (item.getUnitPrice() < 0) {
                throw new IllegalArgumentException("Line item unit price cannot be negative");
            }
        }
    }

    private static String buildReceipt(List<LineItem> lineItems, double subtotal,
            double discount, double preVatTotal, double vat, double total) {
        StringBuilder receipt = new StringBuilder();
        receipt.append("=== RECEIPT ===\n");

        for (LineItem item : lineItems) {
            receipt.append(String.format("%s x%d @ €%.2f = €%.2f\n",
                    item.getDescription(), item.getQuantity(), item.getUnitPrice(), item.getTotal()));
        }

        receipt.append("\n");
        receipt.append(String.format("Subtotal:     €%.2f\n", subtotal));

        if (discount > 0) {
            receipt.append(String.format("Discount:     -€%.2f\n", discount));
        }

        receipt.append(String.format("Subtotal (after discount): €%.2f\n", preVatTotal));
        receipt.append(String.format("VAT (21%%):    €%.2f\n", vat));
        receipt.append(String.format("Total:        €%.2f\n", total));

        return receipt.toString();
    }

    public static void main(String[] args) {
        List<LineItem> order = new ArrayList<>();
        order.add(new LineItem("Widget A", 2, 30.0));
        order.add(new LineItem("Widget B", 3, 20.0));
        order.add(new LineItem("Service", 1, 10.0));

        System.out.println(processOrder(order));
    }
}
```

**Assumptions:**
- Line items consist of description, quantity, and unit price
- Validation rejects null/empty orders, non-positive quantities, negative prices
- 10% discount applies when pre-VAT subtotal exceeds €100; discount reduces the base before VAT calculation
- Receipt shows itemized lines, subtotal, discount (if applicable), pre-discount subtotal, VAT amount, and final total
- All prices in EUR, formatted to 2 decimal places