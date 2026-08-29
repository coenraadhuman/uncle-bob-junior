import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Order {
    public static class LineItem {
        private final String name;
        private final int quantity;
        private final double unitPrice;

        public LineItem(String name, int quantity, double unitPrice) {
            this.name = Objects.requireNonNull(name, "Item name cannot be null");
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }

        public void validate() {
            if (name.isBlank()) {
                throw new IllegalArgumentException("Item name cannot be empty");
            }
            if (quantity <= 0) {
                throw new IllegalArgumentException("Quantity must be positive: " + name);
            }
            if (unitPrice < 0) {
                throw new IllegalArgumentException("Unit price cannot be negative: " + name);
            }
        }

        public double getLineTotal() {
            return quantity * unitPrice;
        }

        public String getName() {
            return name;
        }

        public int getQuantity() {
            return quantity;
        }

        public double getUnitPrice() {
            return unitPrice;
        }
    }

    private final List<LineItem> items = new ArrayList<>();

    public void addItem(LineItem item) {
        item.validate();
        items.add(item);
    }

    public double getPreVatTotal() {
        return items.stream().mapToDouble(LineItem::getLineTotal).sum();
    }

    public double getDiscount() {
        double preVatTotal = getPreVatTotal();
        return preVatTotal > 100 ? preVatTotal * 0.10 : 0;
    }

    public double getSubtotalAfterDiscount() {
        return getPreVatTotal() - getDiscount();
    }

    public double getVat() {
        return getSubtotalAfterDiscount() * 0.21;
    }

    public double getTotal() {
        return getSubtotalAfterDiscount() + getVat();
    }

    public String getReceipt() {
        StringBuilder receipt = new StringBuilder();
        receipt.append("=== ORDER RECEIPT ===\n");
        
        for (LineItem item : items) {
            receipt.append(String.format("%s x%d @ €%.2f = €%.2f\n",
                    item.getName(), item.getQuantity(), item.getUnitPrice(), item.getLineTotal()));
        }
        
        receipt.append("\n");
        receipt.append(String.format("Subtotal:        €%.2f\n", getPreVatTotal()));
        
        if (getDiscount() > 0) {
            receipt.append(String.format("Discount (10%%):  -€%.2f\n", getDiscount()));
        }
        
        receipt.append(String.format("Subtotal:        €%.2f\n", getSubtotalAfterDiscount()));
        receipt.append(String.format("VAT (21%%):       €%.2f\n", getVat()));
        receipt.append("---------------------\n");
        receipt.append(String.format("TOTAL:           €%.2f\n", getTotal()));
        
        return receipt.toString();
    }

    public static void main(String[] args) {
        Order order = new Order();
        order.addItem(new LineItem("Widget", 3, 25.50));
        order.addItem(new LineItem("Gadget", 2, 15.00));
        order.addItem(new LineItem("Doohickey", 1, 45.00));

        System.out.println(order.getReceipt());
    }
}
