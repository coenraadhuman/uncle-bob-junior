import java.util.ArrayList;
import java.util.List;

public class OrderProcessor {
    
    static class LineItem {
        String product;
        int quantity;
        double unitPrice;
        
        public LineItem(String product, int quantity, double unitPrice) {
            this.product = product;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }
    }
    
    public static void main(String[] args) {
        List<LineItem> items = new ArrayList<>();
        items.add(new LineItem("Widget", 5, 25.00));
        items.add(new LineItem("Gadget", 3, 15.50));
        
        String receipt = processOrder(items);
        System.out.println(receipt);
    }
    
    public static String processOrder(List<LineItem> items) {
        // Validate line items
        for (LineItem item : items) {
            if (item.quantity <= 0) {
                throw new IllegalArgumentException("Quantity must be positive: " + item.product);
            }
            if (item.unitPrice < 0) {
                throw new IllegalArgumentException("Unit price cannot be negative: " + item.product);
            }
        }
        
        // Calculate pre-VAT total
        double preVatTotal = 0;
        for (LineItem item : items) {
            preVatTotal += item.quantity * item.unitPrice;
        }
        
        // Apply 10% discount if pre-VAT total exceeds 100 euros
        double discount = 0;
        if (preVatTotal > 100) {
            discount = preVatTotal * 0.10;
        }
        
        double subtotalAfterDiscount = preVatTotal - discount;
        
        // Calculate VAT (21%)
        double vat = subtotalAfterDiscount * 0.21;
        
        // Calculate final total
        double finalTotal = subtotalAfterDiscount + vat;
        
        // Build receipt string
        StringBuilder receipt = new StringBuilder();
        receipt.append("=== ORDER RECEIPT ===\n");
        for (LineItem item : items) {
            double lineTotal = item.quantity * item.unitPrice;
            receipt.append(String.format("%s x%d @ €%.2f = €%.2f\n", 
                item.product, item.quantity, item.unitPrice, lineTotal));
        }
        receipt.append(String.format("\nSubtotal: €%.2f\n", preVatTotal));
        if (discount > 0) {
            receipt.append(String.format("Discount (10%%): -€%.2f\n", discount));
        }
        receipt.append(String.format("Subtotal after discount: €%.2f\n", subtotalAfterDiscount));
        receipt.append(String.format("VAT (21%%): €%.2f\n", vat));
        receipt.append(String.format("===================\n"));
        receipt.append(String.format("Total: €%.2f\n", finalTotal));
        
        return receipt.toString();
    }
}
