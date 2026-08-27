import java.util.ArrayList;
import java.util.List;

public class OrderProcessor {

    static class LineItem {
        String productName;
        int quantity;
        double unitPrice;

        LineItem(String productName, int quantity, double unitPrice) {
            this.productName = productName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }

        double getSubtotal() {
            return quantity * unitPrice;
        }
    }

    static class Order {
        List<LineItem> items;

        Order() {
            this.items = new ArrayList<>();
        }

        void addItem(String productName, int quantity, double unitPrice) {
            items.add(new LineItem(productName, quantity, unitPrice));
        }

        String processAndGenerateReceipt() {
            validateLineItems();
            return generateReceipt();
        }

        void validateLineItems() {
            for (LineItem item : items) {
                if (item.quantity <= 0) {
                    throw new IllegalArgumentException(
                        "Invalid quantity for " + item.productName + ": " + item.quantity);
                }
                if (item.unitPrice < 0) {
                    throw new IllegalArgumentException(
                        "Invalid price for " + item.productName + ": " + item.unitPrice);
                }
            }
        }

        double calculateSubtotal() {
            return items.stream().mapToDouble(LineItem::getSubtotal).sum();
        }

        double calculateTotal() {
            double subtotal = calculateSubtotal();
            double discount = subtotal > 100 ? subtotal * 0.10 : 0;
            double discountedSubtotal = subtotal - discount;
            double vat = discountedSubtotal * 0.21;
            return discountedSubtotal + vat;
        }

        String generateReceipt() {
            double subtotal = calculateSubtotal();
            double discount = subtotal > 100 ? subtotal * 0.10 : 0;
            double discountedSubtotal = subtotal - discount;
            double vat = discountedSubtotal * 0.21;
            double total = discountedSubtotal + vat;

            StringBuilder receipt = new StringBuilder();
            receipt.append("========== RECEIPT ==========\n");
            for (LineItem item : items) {
                receipt.append(String.format("%-20s %d x €%.2f = €%.2f\n",
                    item.productName, item.quantity, item.unitPrice, item.getSubtotal()));
            }
            receipt.append("-----------------------------\n");
            receipt.append(String.format("Subtotal:               €%.2f\n", subtotal));
            if (discount > 0) {
                receipt.append(String.format("Discount (10%%):        -€%.2f\n", discount));
            }
            receipt.append(String.format("Subtotal after discount: €%.2f\n", discountedSubtotal));
            receipt.append(String.format("VAT (21%%):              €%.2f\n", vat));
            receipt.append("-----------------------------\n");
            receipt.append(String.format("TOTAL:                  €%.2f\n", total));
            receipt.append("=============================\n");
            return receipt.toString();
        }
    }

    public static void main(String[] args) {
        Order order = new Order();
        order.addItem("Coffee", 2, 4.50);
        order.addItem("Croissant", 1, 3.25);
        order.addItem("Sandwich", 3, 8.00);

        try {
            String receipt = order.processAndGenerateReceipt();
            System.out.println(receipt);
            System.out.printf("Final Total: €%.2f%n", order.calculateTotal());
        } catch (IllegalArgumentException e) {
            System.out.println("Validation error: " + e.getMessage());
        }
    }
}
