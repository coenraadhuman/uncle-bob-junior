import java.util.*;

class OrderItem {
    String productName;
    double unitPrice;
    int quantity;

    OrderItem(String productName, double unitPrice, int quantity) {
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }
}

class Order {
    private List<OrderItem> items;
    private static final double VAT_RATE = 0.21;
    private static final double DISCOUNT_RATE = 0.10;
    private static final double DISCOUNT_THRESHOLD = 100.0;

    Order(List<OrderItem> items) {
        this.items = items;
    }

    void validate() throws IllegalArgumentException {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        for (OrderItem item : items) {
            if (item.productName == null || item.productName.isEmpty()) {
                throw new IllegalArgumentException("Product name cannot be empty");
            }
            if (item.unitPrice <= 0) {
                throw new IllegalArgumentException("Unit price must be positive: " + item.productName);
            }
            if (item.quantity <= 0) {
                throw new IllegalArgumentException("Quantity must be positive: " + item.productName);
            }
        }
    }

    private double calculateSubtotal() {
        double subtotal = 0;
        for (OrderItem item : items) {
            subtotal += item.unitPrice * item.quantity;
        }
        return subtotal;
    }

    private double calculateDiscountAmount(double subtotal) {
        return subtotal > DISCOUNT_THRESHOLD ? subtotal * DISCOUNT_RATE : 0;
    }

    String generateReceipt() {
        validate();

        double subtotal = calculateSubtotal();
        double discountAmount = calculateDiscountAmount(subtotal);
        double preVatTotal = subtotal - discountAmount;
        double vatAmount = preVatTotal * VAT_RATE;
        double total = preVatTotal + vatAmount;

        StringBuilder receipt = new StringBuilder();
        receipt.append("===== RECEIPT =====\n");

        for (OrderItem item : items) {
            double itemTotal = item.unitPrice * item.quantity;
            receipt.append(String.format("%s x%d @ €%.2f: €%.2f\n",
                    item.productName, item.quantity, item.unitPrice, itemTotal));
        }

        receipt.append("\nSubtotal: €").append(String.format("%.2f", subtotal)).append("\n");

        if (discountAmount > 0) {
            receipt.append("Discount (10%): -€").append(String.format("%.2f", discountAmount)).append("\n");
        }

        receipt.append("Pre-VAT Total: €").append(String.format("%.2f", preVatTotal)).append("\n");
        receipt.append("VAT (21%): €").append(String.format("%.2f", vatAmount)).append("\n");
        receipt.append("Total: €").append(String.format("%.2f", total)).append("\n");
        receipt.append("==================\n");

        return receipt.toString();
    }

    public static void main(String[] args) {
        List<OrderItem> items = Arrays.asList(
                new OrderItem("Widget", 25.00, 2),
                new OrderItem("Gadget", 30.00, 2),
                new OrderItem("Tool", 15.00, 1)
        );

        Order order = new Order(items);
        System.out.println(order.generateReceipt());
    }
}
