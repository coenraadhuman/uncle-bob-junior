import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

class LineItem {
    private String description;
    private int quantity;
    private double unitPrice;

    public LineItem(String description, int quantity, double unitPrice) {
        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public boolean isValid() {
        return quantity > 0 && unitPrice > 0;
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

    public double getLineTotal() {
        return quantity * unitPrice;
    }
}

class Order {
    private List<LineItem> lineItems;
    private static final double VAT_RATE = 0.21;
    private static final double DISCOUNT_RATE = 0.10;
    private static final double DISCOUNT_THRESHOLD = 100.0;

    public Order(List<LineItem> lineItems) {
        this.lineItems = lineItems;
    }

    public boolean validateLineItems() {
        if (lineItems == null || lineItems.isEmpty()) {
            return false;
        }
        return lineItems.stream().allMatch(LineItem::isValid);
    }

    public double calculatePreVatTotal() {
        return lineItems.stream()
            .mapToDouble(LineItem::getLineTotal)
            .sum();
    }

    public double calculateDiscount(double preVatTotal) {
        return preVatTotal > DISCOUNT_THRESHOLD ? preVatTotal * DISCOUNT_RATE : 0.0;
    }

    public double calculateSubtotal(double preVatTotal, double discount) {
        return preVatTotal - discount;
    }

    public double calculateVat(double subtotal) {
        return subtotal * VAT_RATE;
    }

    public double calculateTotal(double preVatTotal) {
        double discount = calculateDiscount(preVatTotal);
        double subtotal = calculateSubtotal(preVatTotal, discount);
        double vat = calculateVat(subtotal);
        return subtotal + vat;
    }

    public String generateReceipt() {
        if (!validateLineItems()) {
            return "ERROR: Invalid order - check line items";
        }

        double preVatTotal = calculatePreVatTotal();
        double discount = calculateDiscount(preVatTotal);
        double subtotal = calculateSubtotal(preVatTotal, discount);
        double vat = calculateVat(subtotal);
        double total = subtotal + vat;

        StringBuilder receipt = new StringBuilder();
        receipt.append("=".repeat(50)).append("\n");
        receipt.append("RECEIPT\n");
        receipt.append("=".repeat(50)).append("\n");
        receipt.append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"))).append("\n\n");

        receipt.append(String.format("%-30s %10s %10s\n", "Item", "Qty", "Total"));
        receipt.append("-".repeat(50)).append("\n");

        for (LineItem item : lineItems) {
            receipt.append(String.format("%-30s %10d €%9.2f\n",
                item.getDescription(),
                item.getQuantity(),
                item.getLineTotal()));
        }

        receipt.append("-".repeat(50)).append("\n");
        receipt.append(String.format("%-40s €%9.2f\n", "Pre-VAT Total:", preVatTotal));

        if (discount > 0) {
            receipt.append(String.format("%-40s €%9.2f\n", "Discount (10%):", -discount));
        }

        receipt.append(String.format("%-40s €%9.2f\n", "Subtotal:", subtotal));
        receipt.append(String.format("%-40s €%9.2f\n", "VAT (21%):", vat));
        receipt.append("=".repeat(50)).append("\n");
        receipt.append(String.format("%-40s €%9.2f\n", "TOTAL:", total));
        receipt.append("=".repeat(50)).append("\n");

        return receipt.toString();
    }
}

public class OrderProcessor {
    public static void main(String[] args) {
        List<LineItem> items = new ArrayList<>();
        items.add(new LineItem("Coffee", 2, 3.50));
        items.add(new LineItem("Sandwich", 1, 8.75));
        items.add(new LineItem("Pastry", 3, 4.25));
        items.add(new LineItem("Juice", 1, 3.00));

        Order order = new Order(items);
        System.out.println(order.generateReceipt());

        System.out.println("\n--- Order Summary ---");
        double preVatTotal = order.calculatePreVatTotal();
        double discount = order.calculateDiscount(preVatTotal);
        double total = order.calculateTotal(preVatTotal);
        System.out.printf("Pre-VAT Total: €%.2f\n", preVatTotal);
        System.out.printf("Discount Applied: €%.2f\n", discount);
        System.out.printf("Final Total (with VAT): €%.2f\n", total);
    }
}
