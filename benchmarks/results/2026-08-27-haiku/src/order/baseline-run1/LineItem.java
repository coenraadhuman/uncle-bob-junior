import java.util.ArrayList;
import java.util.List;

class LineItem {
    String description;
    double price;
    int quantity;

    LineItem(String description, double price, int quantity) {
        this.description = description;
        this.price = price;
        this.quantity = quantity;
    }

    double getSubtotal() {
        return price * quantity;
    }
}

class Order {
    private List<LineItem> items;
    private static final double VAT_RATE = 0.21;
    private static final double DISCOUNT_RATE = 0.10;
    private static final double DISCOUNT_THRESHOLD = 100.0;

    Order() {
        this.items = new ArrayList<>();
    }

    void addLineItem(String description, double price, int quantity) {
        if (price < 0 || quantity < 0) {
            throw new IllegalArgumentException("Price and quantity must be non-negative");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be empty");
        }
        items.add(new LineItem(description, price, quantity));
    }

    String generateReceipt() {
        if (items.isEmpty()) {
            throw new IllegalStateException("Cannot generate receipt for empty order");
        }

        double preVatTotal = 0;
        StringBuilder receipt = new StringBuilder();
        receipt.append("========== RECEIPT ==========\n");

        for (LineItem item : items) {
            double subtotal = item.getSubtotal();
            preVatTotal += subtotal;
            receipt.append(String.format("%-20s %5d x €%7.2f = €%8.2f\n",
                    item.description, item.quantity, item.price, subtotal));
        }

        receipt.append("-----------------------------\n");
        receipt.append(String.format("Subtotal:           €%8.2f\n", preVatTotal));

        double discount = 0;
        if (preVatTotal > DISCOUNT_THRESHOLD) {
            discount = preVatTotal * DISCOUNT_RATE;
            receipt.append(String.format("Discount (10%%):     -€%8.2f\n", discount));
        }

        double afterDiscount = preVatTotal - discount;
        double vat = afterDiscount * VAT_RATE;
        double total = afterDiscount + vat;

        receipt.append(String.format("VAT (21%%):          €%8.2f\n", vat));
        receipt.append("-----------------------------\n");
        receipt.append(String.format("Total:              €%8.2f\n", total));
        receipt.append("=============================\n");

        return receipt.toString();
    }
}

public class OrderProcessor {
    public static void main(String[] args) {
        Order order = new Order();
        order.addLineItem("Laptop", 899.99, 1);
        order.addLineItem("Mouse", 29.99, 2);
        order.addLineItem("Cable", 12.50, 1);

        System.out.println(order.generateReceipt());

        // Example 2: Order below discount threshold
        Order order2 = new Order();
        order2.addLineItem("Book", 19.99, 2);
        order2.addLineItem("Pen", 5.00, 3);

        System.out.println(order2.generateReceipt());
    }
}
