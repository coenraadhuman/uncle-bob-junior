import java.util.ArrayList;
import java.util.List;

public class Order {
    private List<LineItem> items;
    private static final double VAT_RATE = 0.21;
    private static final double DISCOUNT_RATE = 0.10;
    private static final double DISCOUNT_THRESHOLD = 100.0;

    public Order() {
        this.items = new ArrayList<>();
    }

    public void addLineItem(String name, double unitPrice, int quantity) {
        LineItem item = new LineItem(name, unitPrice, quantity);
        item.validate();
        this.items.add(item);
    }

    public String processOrder() {
        validateOrder();
        return generateReceipt();
    }

    private void validateOrder() {
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one line item");
        }
    }

    private double calculateSubtotal() {
        return items.stream()
                .mapToDouble(LineItem::getTotal)
                .sum();
    }

    private double calculateDiscount(double subtotal) {
        return subtotal > DISCOUNT_THRESHOLD ? subtotal * DISCOUNT_RATE : 0.0;
    }

    private double calculateVat(double subtotalAfterDiscount) {
        return subtotalAfterDiscount * VAT_RATE;
    }

    private String generateReceipt() {
        StringBuilder receipt = new StringBuilder();
        receipt.append("========== RECEIPT ==========\n");

        for (LineItem item : items) {
            receipt.append(String.format("%s x%d @ €%.2f = €%.2f\n",
                    item.getName(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getTotal()));
        }

        double subtotal = calculateSubtotal();
        double discount = calculateDiscount(subtotal);
        double subtotalAfterDiscount = subtotal - discount;
        double vat = calculateVat(subtotalAfterDiscount);
        double total = subtotalAfterDiscount + vat;

        receipt.append("-----------------------------\n");
        receipt.append(String.format("Subtotal: €%.2f\n", subtotal));

        if (discount > 0) {
            receipt.append(String.format("Discount (10%%): -€%.2f\n", discount));
        }

        receipt.append(String.format("Subtotal after discount: €%.2f\n", subtotalAfterDiscount));
        receipt.append(String.format("VAT (21%%): €%.2f\n", vat));
        receipt.append("-----------------------------\n");
        receipt.append(String.format("TOTAL: €%.2f\n", total));
        receipt.append("=============================\n");

        return receipt.toString();
    }

    private static class LineItem {
        private String name;
        private double unitPrice;
        private int quantity;

        public LineItem(String name, double unitPrice, int quantity) {
            this.name = name;
            this.unitPrice = unitPrice;
            this.quantity = quantity;
        }

        public void validate() {
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Item name cannot be empty");
            }
            if (unitPrice <= 0) {
                throw new IllegalArgumentException("Unit price must be positive");
            }
            if (quantity <= 0) {
                throw new IllegalArgumentException("Quantity must be positive");
            }
        }

        public double getTotal() {
            return unitPrice * quantity;
        }

        public String getName() {
            return name;
        }

        public double getUnitPrice() {
            return unitPrice;
        }

        public int getQuantity() {
            return quantity;
        }
    }
}
