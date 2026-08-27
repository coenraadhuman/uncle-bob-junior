import java.util.*;
import java.text.DecimalFormat;

public class OrderProcessor {
    static class LineItem {
        String productName;
        int quantity;
        double unitPrice;

        public LineItem(String productName, int quantity, double unitPrice) {
            this.productName = productName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }

        double getSubtotal() {
            return quantity * unitPrice;
        }
    }

    private static final double VAT_RATE = 0.21;
    private static final double DISCOUNT_RATE = 0.10;
    private static final double DISCOUNT_THRESHOLD = 100.0;

    public static void main(String[] args) {
        List<LineItem> items = new ArrayList<>();
        items.add(new LineItem("Widget A", 5, 15.00));
        items.add(new LineItem("Widget B", 3, 20.00));
        items.add(new LineItem("Service Fee", 1, 50.00));

        String receipt = processOrder(items);
        System.out.println(receipt);
    }

    public static String processOrder(List<LineItem> lineItems) {
        validateLineItems(lineItems);

        double preVatTotal = calculatePreVatTotal(lineItems);
        double discount = preVatTotal > DISCOUNT_THRESHOLD ? preVatTotal * DISCOUNT_RATE : 0.0;
        double discountedTotal = preVatTotal - discount;
        double vat = discountedTotal * VAT_RATE;
        double finalTotal = discountedTotal + vat;

        return generateReceipt(lineItems, preVatTotal, discount, discountedTotal, vat, finalTotal);
    }

    private static void validateLineItems(List<LineItem> lineItems) {
        if (lineItems == null || lineItems.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one line item");
        }
        for (LineItem item : lineItems) {
            if (item.quantity <= 0) {
                throw new IllegalArgumentException("Quantity must be positive for: " + item.productName);
            }
            if (item.unitPrice < 0) {
                throw new IllegalArgumentException("Unit price cannot be negative for: " + item.productName);
            }
        }
    }

    private static double calculatePreVatTotal(List<LineItem> lineItems) {
        return lineItems.stream().mapToDouble(LineItem::getSubtotal).sum();
    }

    private static String generateReceipt(List<LineItem> items, double preVatTotal, 
                                          double discount, double discountedTotal, 
                                          double vat, double finalTotal) {
        DecimalFormat df = new DecimalFormat("0.00");
        StringBuilder receipt = new StringBuilder();

        receipt.append("=== ORDER RECEIPT ===\n");
        receipt.append("\nLine Items:\n");
        for (LineItem item : items) {
            receipt.append(String.format("  %-20s | Qty: %3d | Unit: €%8s | Subtotal: €%8s\n",
                    item.productName, item.quantity, df.format(item.unitPrice), 
                    df.format(item.getSubtotal())));
        }

        receipt.append("\n-------------------\n");
        receipt.append(String.format("Pre-VAT Total:       €%8s\n", df.format(preVatTotal)));
        if (discount > 0) {
            receipt.append(String.format("Discount (10%%):     -€%8s\n", df.format(discount)));
        }
        receipt.append(String.format("Subtotal:            €%8s\n", df.format(discountedTotal)));
        receipt.append(String.format("VAT (21%%):           €%8s\n", df.format(vat)));
        receipt.append("-------------------\n");
        receipt.append(String.format("TOTAL:               €%8s\n", df.format(finalTotal)));

        return receipt.toString();
    }
}
