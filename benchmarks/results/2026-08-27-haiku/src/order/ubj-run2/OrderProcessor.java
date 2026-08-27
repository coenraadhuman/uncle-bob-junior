import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OrderProcessor {
    private static final double VAT_RATE = 0.21;
    private static final double DISCOUNT_RATE = 0.10;
    private static final double DISCOUNT_THRESHOLD = 100.0;

    public static class LineItem {
        private final String description;
        private final int quantity;
        private final double unitPrice;

        public LineItem(String description, int quantity, double unitPrice) {
            validateLineItem(description, quantity, unitPrice);
            this.description = description;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }

        private void validateLineItem(String description, int quantity, double unitPrice) {
            if (description == null || description.isBlank()) {
                throw new IllegalArgumentException("Line item description cannot be empty");
            }
            if (quantity <= 0) {
                throw new IllegalArgumentException("Line item quantity must be positive");
            }
            if (unitPrice < 0) {
                throw new IllegalArgumentException("Line item unit price cannot be negative");
            }
        }

        public double lineTotal() {
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

    public static class Order {
        private final List<LineItem> items;

        public Order() {
            this.items = new ArrayList<>();
        }

        public void addLineItem(LineItem item) {
            Objects.requireNonNull(item, "Line item cannot be null");
            items.add(item);
        }

        public double subtotal() {
            return items.stream().mapToDouble(LineItem::lineTotal).sum();
        }

        public double discountAmount() {
            double subtotal = subtotal();
            return subtotal > DISCOUNT_THRESHOLD ? subtotal * DISCOUNT_RATE : 0.0;
        }

        public double subtotalAfterDiscount() {
            return subtotal() - discountAmount();
        }

        public double vatAmount() {
            return subtotalAfterDiscount() * VAT_RATE;
        }

        public double total() {
            return subtotalAfterDiscount() + vatAmount();
        }

        public String generateReceipt() {
            if (items.isEmpty()) {
                throw new IllegalStateException("Cannot generate receipt for empty order");
            }

            StringBuilder receipt = new StringBuilder();
            receipt.append("========== RECEIPT ==========\n");

            for (LineItem item : items) {
                receipt.append(String.format("%-25s %3d x €%.2f = €%.2f\n",
                    item.getDescription(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.lineTotal()));
            }

            receipt.append("-----------------------------\n");
            receipt.append(String.format("Subtotal: €%.2f\n", subtotal()));

            if (discountAmount() > 0) {
                receipt.append(String.format("Discount (10%%): -€%.2f\n", discountAmount()));
            }

            receipt.append(String.format("Subtotal after discount: €%.2f\n", subtotalAfterDiscount()));
            receipt.append(String.format("VAT (21%%): €%.2f\n", vatAmount()));
            receipt.append("-----------------------------\n");
            receipt.append(String.format("Total: €%.2f\n", total()));
            receipt.append("=============================\n");

            return receipt.toString();
        }
    }

    public static void main(String[] args) {
        Order order = new Order();
        order.addLineItem(new LineItem("Widget", 2, 45.00));
        order.addLineItem(new LineItem("Gadget", 1, 20.00));
        order.addLineItem(new LineItem("Doohickey", 3, 15.00));

        System.out.println(order.generateReceipt());
    }
}
