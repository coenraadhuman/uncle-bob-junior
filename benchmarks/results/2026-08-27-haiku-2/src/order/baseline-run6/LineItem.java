import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

class LineItem {
    private final String description;
    private final BigDecimal price;
    private final int quantity;

    public LineItem(String description, BigDecimal price, int quantity) {
        this.description = description;
        this.price = price;
        this.quantity = quantity;
    }

    public BigDecimal getLineTotal() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }
}

class Order {
    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100");
    
    private final List<LineItem> items = new ArrayList<>();

    public void addItem(LineItem item) {
        items.add(item);
    }

    public void validate() throws IllegalArgumentException {
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }
        for (LineItem item : items) {
            if (item.getQuantity() <= 0) {
                throw new IllegalArgumentException("Item quantity must be positive: " + item.getDescription());
            }
            if (item.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Item price must be positive: " + item.getDescription());
            }
        }
    }

    public String generateReceipt() {
        validate();

        BigDecimal subtotal = items.stream()
            .map(LineItem::getLineTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discount = subtotal.compareTo(DISCOUNT_THRESHOLD) > 0
            ? subtotal.multiply(DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;

        BigDecimal subtotalAfterDiscount = subtotal.subtract(discount);
        BigDecimal vat = subtotalAfterDiscount.multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotalAfterDiscount.add(vat);

        StringBuilder receipt = new StringBuilder();
        receipt.append("========== RECEIPT ==========\n");
        
        for (LineItem item : items) {
            BigDecimal lineTotal = item.getLineTotal();
            receipt.append(String.format("%s @ €%.2f x %d = €%.2f\n",
                item.getDescription(), item.getPrice(), item.getQuantity(), lineTotal));
        }
        
        receipt.append("------------------------------\n");
        receipt.append(String.format("Subtotal:              €%.2f\n", subtotal));
        
        if (discount.compareTo(BigDecimal.ZERO) > 0) {
            receipt.append(String.format("Discount (10%%):        -€%.2f\n", discount));
        }
        
        receipt.append(String.format("Before VAT:            €%.2f\n", subtotalAfterDiscount));
        receipt.append(String.format("VAT (21%%):             €%.2f\n", vat));
        receipt.append("------------------------------\n");
        receipt.append(String.format("TOTAL:                 €%.2f\n", total));
        receipt.append("=============================\n");

        return receipt.toString();
    }
}

public class OrderProcessor {
    public static void main(String[] args) {
        // Example 1: Order with discount (total > 100 before VAT)
        Order order1 = new Order();
        order1.addItem(new LineItem("Laptop", new BigDecimal("750.00"), 1));
        order1.addItem(new LineItem("Mouse", new BigDecimal("25.00"), 2));
        
        System.out.println("=== Order 1 (with discount) ===");
        System.out.println(order1.generateReceipt());

        // Example 2: Order without discount (total < 100 before VAT)
        Order order2 = new Order();
        order2.addItem(new LineItem("Coffee", new BigDecimal("5.50"), 3));
        order2.addItem(new LineItem("Pastry", new BigDecimal("3.25"), 2));
        
        System.out.println("=== Order 2 (no discount) ===");
        System.out.println(order2.generateReceipt());

        // Example 3: Validation error handling
        try {
            Order order3 = new Order();
            order3.addItem(new LineItem("Product", new BigDecimal("-10.00"), 1));
            order3.generateReceipt();
        } catch (IllegalArgumentException e) {
            System.out.println("Validation error: " + e.getMessage());
        }
    }
}
