import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class LineItem {
    private final String description;
    private final double price;
    private final int quantity;

    public LineItem(String description, double price, int quantity) {
        this.description = Objects.requireNonNull(description);
        this.price = price;
        this.quantity = quantity;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getTotal() {
        return price * quantity;
    }
}

class Order {
    private final List<LineItem> lineItems;

    public Order() {
        this.lineItems = new ArrayList<>();
    }

    public void addLineItem(LineItem item) {
        if (item.getPrice() < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        if (item.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        lineItems.add(item);
    }

    public String generateReceipt() {
        if (lineItems.isEmpty()) {
            throw new IllegalStateException("Order has no line items");
        }

        double subtotal = calculateSubtotal();
        double discount = subtotal > 100 ? subtotal * 0.10 : 0;
        double discountedSubtotal = subtotal - discount;
        double vat = discountedSubtotal * 0.21;
        double total = discountedSubtotal + vat;

        StringBuilder receipt = new StringBuilder();
        receipt.append("========== RECEIPT ==========\n");
        
        for (LineItem item : lineItems) {
            receipt.append(String.format("%-25s %8.2f x %2d = %8.2f EUR\n",
                    item.getDescription(), item.getPrice(), item.getQuantity(), item.getTotal()));
        }

        receipt.append("-----------------------------\n");
        receipt.append(String.format("Subtotal:                       %8.2f EUR\n", subtotal));

        if (discount > 0) {
            receipt.append(String.format("Discount (10%%):               -%8.2f EUR\n", discount));
        }

        receipt.append(String.format("Subtotal after discount:        %8.2f EUR\n", discountedSubtotal));
        receipt.append(String.format("VAT (21%%):                      %8.2f EUR\n", vat));
        receipt.append("=============================\n");
        receipt.append(String.format("Total:                          %8.2f EUR\n", total));
        receipt.append("=============================\n");

        return receipt.toString();
    }

    private double calculateSubtotal() {
        return lineItems.stream()
                .mapToDouble(LineItem::getTotal)
                .sum();
    }
}

public class OrderProcessor {
    public static void main(String[] args) {
        Order order = new Order();
        order.addLineItem(new LineItem("Apples", 2.50, 10));
        order.addLineItem(new LineItem("Bread", 1.80, 5));
        order.addLineItem(new LineItem("Milk", 3.20, 3));
        order.addLineItem(new LineItem("Cheese", 8.50, 2));

        System.out.println(order.generateReceipt());
    }
}
