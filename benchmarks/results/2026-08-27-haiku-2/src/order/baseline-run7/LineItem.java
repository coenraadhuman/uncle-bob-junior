import java.util.ArrayList;
import java.util.List;

class LineItem {
    private String product;
    private double unitPrice;
    private int quantity;

    public LineItem(String product, double unitPrice, int quantity) {
        this.product = product;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    public String getProduct() {
        return product;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getLineTotal() {
        return unitPrice * quantity;
    }
}

class Order {
    private List<LineItem> lineItems = new ArrayList<>();
    private static final double VAT_RATE = 0.21;
    private static final double DISCOUNT_RATE = 0.10;
    private static final double DISCOUNT_THRESHOLD = 100.0;

    public void addLineItem(LineItem item) {
        lineItems.add(item);
    }

    public void validate() throws IllegalArgumentException {
        if (lineItems.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one line item");
        }

        for (LineItem item : lineItems) {
            if (item.getProduct() == null || item.getProduct().isEmpty()) {
                throw new IllegalArgumentException("Product name cannot be null or empty");
            }
            if (item.getUnitPrice() < 0) {
                throw new IllegalArgumentException("Unit price cannot be negative: " + item.getProduct());
            }
            if (item.getQuantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be positive: " + item.getProduct());
            }
        }
    }

    public double calculateSubtotal() {
        return lineItems.stream().mapToDouble(LineItem::getLineTotal).sum();
    }

    public double calculateDiscount(double subtotal) {
        return subtotal > DISCOUNT_THRESHOLD ? subtotal * DISCOUNT_RATE : 0.0;
    }

    public double calculateTotal() {
        double subtotal = calculateSubtotal();
        double discount = calculateDiscount(subtotal);
        double afterDiscount = subtotal - discount;
        double vat = afterDiscount * VAT_RATE;
        return afterDiscount + vat;
    }

    public String generateReceipt() {
        validate();

        double subtotal = calculateSubtotal();
        double discount = calculateDiscount(subtotal);
        double afterDiscount = subtotal - discount;
        double vat = afterDiscount * VAT_RATE;
        double total = afterDiscount + vat;

        StringBuilder receipt = new StringBuilder();
        receipt.append("========== RECEIPT ==========\n");
        receipt.append("Items:\n");

        for (LineItem item : lineItems) {
            receipt.append(String.format("  %-20s x%d @ €%.2f = €%.2f\n",
                    item.getProduct(), item.getQuantity(), item.getUnitPrice(), item.getLineTotal()));
        }

        receipt.append(String.format("\nSubtotal:          €%.2f\n", subtotal));

        if (discount > 0) {
            receipt.append(String.format("Discount (10%):   -€%.2f\n", discount));
        }

        receipt.append(String.format("Subtotal after discount: €%.2f\n", afterDiscount));
        receipt.append(String.format("VAT (21%):         €%.2f\n", vat));
        receipt.append(String.format("TOTAL:             €%.2f\n", total));
        receipt.append("=============================\n");

        return receipt.toString();
    }
}

public class OrderProcessor {
    public static void main(String[] args) {
        // Example usage
        Order order = new Order();
        order.addLineItem(new LineItem("Widget A", 25.50, 2));
        order.addLineItem(new LineItem("Widget B", 30.00, 2));
        order.addLineItem(new LineItem("Widget C", 15.75, 1));

        System.out.println(order.generateReceipt());
    }
}
