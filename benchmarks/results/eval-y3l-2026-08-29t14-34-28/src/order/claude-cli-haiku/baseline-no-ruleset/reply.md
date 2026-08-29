I'll write a complete Java solution for order processing with validation, VAT calculation, discount logic, and receipt generation.

Assumptions:
- Line items need description, quantity, and unit price
- Discount applies to pre-VAT subtotal only
- VAT (21%) is calculated after discount
- Receipt is formatted as a string with all details

```java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

class LineItem {
    private String description;
    private int quantity;
    private BigDecimal unitPrice;

    public LineItem(String description, int quantity, BigDecimal unitPrice) {
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

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}

class Order {
    private List<LineItem> lineItems;
    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100");

    public Order(List<LineItem> lineItems) {
        this.lineItems = lineItems;
    }

    public void validate() throws IllegalArgumentException {
        if (lineItems == null || lineItems.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one line item");
        }

        for (LineItem item : lineItems) {
            if (item.getQuantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be positive for: " + item.getDescription());
            }
            if (item.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Unit price cannot be negative for: " + item.getDescription());
            }
        }
    }

    private BigDecimal getSubtotal() {
        return lineItems.stream()
            .map(LineItem::getTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal getDiscount(BigDecimal subtotal) {
        if (subtotal.compareTo(DISCOUNT_THRESHOLD) > 0) {
            return subtotal.multiply(DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal getVat(BigDecimal discountedSubtotal) {
        return discountedSubtotal.multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);
    }

    public String generateReceipt() {
        validate();

        BigDecimal subtotal = getSubtotal();
        BigDecimal discount = getDiscount(subtotal);
        BigDecimal discountedSubtotal = subtotal.subtract(discount);
        BigDecimal vat = getVat(discountedSubtotal);
        BigDecimal total = discountedSubtotal.add(vat);

        StringBuilder receipt = new StringBuilder();
        receipt.append("===== ORDER RECEIPT =====\n");
        receipt.append("\nLine Items:\n");

        for (LineItem item : lineItems) {
            receipt.append(String.format("  %-30s %3d x €%8.2f = €%8.2f\n",
                item.getDescription(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getTotal()));
        }

        receipt.append("\n" + "-".repeat(50) + "\n");
        receipt.append(String.format("Subtotal:                           €%8.2f\n", subtotal));

        if (discount.compareTo(BigDecimal.ZERO) > 0) {
            receipt.append(String.format("Discount (10%%):                    -€%8.2f\n", discount));
        }

        receipt.append(String.format("Subtotal after discount:            €%8.2f\n", discountedSubtotal));
        receipt.append(String.format("VAT (21%%):                          €%8.2f\n", vat));
        receipt.append("-".repeat(50) + "\n");
        receipt.append(String.format("TOTAL:                              €%8.2f\n", total));

        return receipt.toString();
    }
}

public class OrderProcessor {
    public static void main(String[] args) {
        List<LineItem> items = new ArrayList<>();
        items.add(new LineItem("Coffee", 2, new BigDecimal("4.50")));
        items.add(new LineItem("Sandwich", 3, new BigDecimal("8.75")));
        items.add(new LineItem("Juice", 4, new BigDecimal("3.25")));

        Order order = new Order(items);
        System.out.println(order.generateReceipt());
    }
}
```

The code provides:
- **LineItem**: represents a product with description, quantity, and price
- **Order**: validates items, calculates subtotal, applies 10% discount if subtotal > €100, calculates 21% VAT on discounted amount, and formats a receipt
- **Main method**: demonstrates with sample items (totaling €127 pre-VAT, triggering the discount)