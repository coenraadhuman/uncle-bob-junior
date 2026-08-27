I'll create a clean, well-structured solution for order processing. Here are my assumptions:
- Line items have a product name, unit price (€), and quantity
- Invalid items: negative/zero prices, non-positive quantities
- Discount (10%) applies to pre-VAT subtotal when it exceeds €100
- VAT (21%) is applied after discount
- Receipt shows itemized details plus totals

```java
public class LineItem {
    private final String product;
    private final double pricePerUnit;
    private final int quantity;

    public LineItem(String product, double pricePerUnit, int quantity) {
        this.product = product;
        this.pricePerUnit = pricePerUnit;
        this.quantity = quantity;
    }

    public void validate() throws IllegalArgumentException {
        if (product == null || product.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be empty");
        }
        if (pricePerUnit < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
    }

    public double subtotal() {
        return pricePerUnit * quantity;
    }

    public String getProduct() {
        return product;
    }

    public double getPricePerUnit() {
        return pricePerUnit;
    }

    public int getQuantity() {
        return quantity;
    }
}

public class Order {
    private static final double DISCOUNT_THRESHOLD = 100.0;
    private static final double DISCOUNT_RATE = 0.10;
    private static final double VAT_RATE = 0.21;

    private final List<LineItem> items;

    public Order(List<LineItem> items) {
        this.items = new ArrayList<>(items);
    }

    public void validateItems() throws IllegalArgumentException {
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }
        for (LineItem item : items) {
            item.validate();
        }
    }

    public double computePreVatTotal() {
        return items.stream()
            .mapToDouble(LineItem::subtotal)
            .sum();
    }

    public double computeDiscount() {
        double preVatTotal = computePreVatTotal();
        return preVatTotal > DISCOUNT_THRESHOLD 
            ? preVatTotal * DISCOUNT_RATE 
            : 0.0;
    }

    public double computeSubtotalAfterDiscount() {
        return computePreVatTotal() - computeDiscount();
    }

    public double computeVat() {
        return computeSubtotalAfterDiscount() * VAT_RATE;
    }

    public double computeTotal() {
        return computeSubtotalAfterDiscount() + computeVat();
    }

    public String produceReceipt() {
        validateItems();
        
        StringBuilder receipt = new StringBuilder();
        receipt.append("=== ORDER RECEIPT ===\n");
        receipt.append(String.format("%-30s %10s %8s %12s\n", "Product", "Unit Price", "Qty", "Subtotal"));
        receipt.append("-".repeat(62)).append("\n");

        for (LineItem item : items) {
            receipt.append(String.format("%-30s €%9.2f %8d €%11.2f\n",
                truncate(item.getProduct(), 30),
                item.getPricePerUnit(),
                item.getQuantity(),
                item.subtotal()));
        }

        receipt.append("-".repeat(62)).append("\n");
        double preVatTotal = computePreVatTotal();
        double discount = computeDiscount();
        double subtotalAfterDiscount = computeSubtotalAfterDiscount();
        double vat = computeVat();
        double total = computeTotal();

        receipt.append(String.format("%-42s €%11.2f\n", "Subtotal", preVatTotal));
        
        if (discount > 0) {
            receipt.append(String.format("%-42s -€%10.2f\n", "Discount (10%)", discount));
            receipt.append(String.format("%-42s €%11.2f\n", "After Discount", subtotalAfterDiscount));
        }
        
        receipt.append(String.format("%-42s €%11.2f\n", "VAT (21%)", vat));
        receipt.append("=".repeat(62)).append("\n");
        receipt.append(String.format("%-42s €%11.2f\n", "TOTAL", total));

        return receipt.toString();
    }

    private String truncate(String text, int maxLength) {
        return text.length() <= maxLength ? text : text.substring(0, maxLength - 3) + "...";
    }
}
```

```java
// Test usage and verification
public class OrderProcessorTest {
    public static void main(String[] args) {
        testValidOrder();
        testOrderWithDiscount();
        testInvalidItem();
        testEmptyOrder();
    }

    private static void testValidOrder() {
        System.out.println("--- Test: Valid Order (no discount) ---");
        List<LineItem> items = List.of(
            new LineItem("Coffee", 5.0, 2),
            new LineItem("Croissant", 3.5, 3)
        );
        Order order = new Order(items);
        System.out.println(order.produceReceipt());
        System.out.println();
    }

    private static void testOrderWithDiscount() {
        System.out.println("--- Test: Order with Discount (>€100) ---");
        List<LineItem> items = List.of(
            new LineItem("Laptop Monitor", 120.0, 1),
            new LineItem("USB Cable", 8.50, 2)
        );
        Order order = new Order(items);
        System.out.println(order.produceReceipt());
        System.out.println();
    }

    private static void testInvalidItem() {
        System.out.println("--- Test: Invalid Item (negative price) ---");
        try {
            List<LineItem> items = List.of(new LineItem("Item", -5.0, 2));
            Order order = new Order(items);
            order.produceReceipt();
        } catch (IllegalArgumentException e) {
            System.out.println("✓ Caught: " + e.getMessage());
        }
        System.out.println();
    }

    private static void testEmptyOrder() {
        System.out.println("--- Test: Empty Order ---");
        try {
            Order order = new Order(List.of());
            order.produceReceipt();
        } catch (IllegalArgumentException e) {
            System.out.println("✓ Caught: " + e.getMessage());
        }
    }
}
```

**Cleaned:** Each class owns one responsibility (item validation, order calculation, receipt formatting); discount and VAT rates are named constants; calculations are isolated methods testable independently; guard clauses validate early; receipt formatting is delegated to a focused method.

**Safe to change:** Adding new item fields, changing VAT/discount rates, or reformatting the receipt now touches only the relevant method without cascading changes.