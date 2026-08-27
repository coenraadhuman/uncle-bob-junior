```java
import java.util.ArrayList;
import java.util.List;

class LineItem {
    private String description;
    private int quantity;
    private double unitPrice;

    LineItem(String description, int quantity, double unitPrice) {
        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    String getDescription() {
        return description;
    }

    int getQuantity() {
        return quantity;
    }

    double getUnitPrice() {
        return unitPrice;
    }

    double getTotal() {
        return quantity * unitPrice;
    }
}

class Order {
    private static final double VAT_RATE = 0.21;
    private static final double DISCOUNT_RATE = 0.10;
    private static final double DISCOUNT_THRESHOLD = 100.0;

    private List<LineItem> items;

    Order() {
        this.items = new ArrayList<>();
    }

    void addItem(LineItem item) {
        validateLineItem(item);
        items.add(item);
    }

    private void validateLineItem(LineItem item) {
        if (item.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (item.getUnitPrice() < 0) {
            throw new IllegalArgumentException("Unit price cannot be negative");
        }
        if (item.getDescription() == null || item.getDescription().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be empty");
        }
    }

    double calculatePreVatTotal() {
        return items.stream().mapToDouble(LineItem::getTotal).sum();
    }

    double calculateDiscount() {
        double preVatTotal = calculatePreVatTotal();
        return preVatTotal > DISCOUNT_THRESHOLD ? preVatTotal * DISCOUNT_RATE : 0;
    }

    double calculateSubtotalAfterDiscount() {
        return calculatePreVatTotal() - calculateDiscount();
    }

    double calculateVat() {
        return calculateSubtotalAfterDiscount() * VAT_RATE;
    }

    double calculateTotal() {
        return calculateSubtotalAfterDiscount() + calculateVat();
    }

    String generateReceipt() {
        double preVatTotal = calculatePreVatTotal();
        double discount = calculateDiscount();
        double subtotalAfterDiscount = calculateSubtotalAfterDiscount();
        double vat = calculateVat();
        double total = calculateTotal();

        StringBuilder receipt = new StringBuilder();
        receipt.append("========== RECEIPT ==========\n");
        
        for (LineItem item : items) {
            receipt.append(String.format("%-20s %2d x €%6.2f = €%7.2f\n",
                item.getDescription(), item.getQuantity(), item.getUnitPrice(), item.getTotal()));
        }
        
        receipt.append("-----------------------------\n");
        receipt.append(String.format("Subtotal:                    €%7.2f\n", preVatTotal));
        
        if (discount > 0) {
            receipt.append(String.format("Discount (10%%):             -€%7.2f\n", discount));
        }
        
        receipt.append(String.format("Subtotal after discount:     €%7.2f\n", subtotalAfterDiscount));
        receipt.append(String.format("VAT (21%%):                   €%7.2f\n", vat));
        receipt.append("=============================\n");
        receipt.append(String.format("Total:                       €%7.2f\n", total));
        
        return receipt.toString();
    }
}

public class OrderProcessor {
    public static void main(String[] args) {
        Order order = new Order();
        
        order.addItem(new LineItem("Widget A", 3, 15.50));
        order.addItem(new LineItem("Widget B", 2, 28.00));
        order.addItem(new LineItem("Service", 1, 35.00));
        
        System.out.println(order.generateReceipt());
        System.out.println("Final Total: €" + String.format("%.2f", order.calculateTotal()));
    }
}
```

**Key assumptions:**
- Line items require a non-empty description, positive quantity, and non-negative unit price
- The 10% discount applies to the pre-VAT subtotal when it exceeds €100
- VAT (21%) is calculated on the already-discounted amount
- The receipt format shows itemized details, calculations, and a formatted total

The example processes 3 items totaling €133.50 pre-VAT, so the 10% discount (€13.35) applies, followed by 21% VAT on the discounted subtotal.