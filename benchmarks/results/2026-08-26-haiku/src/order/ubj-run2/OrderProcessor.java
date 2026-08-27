import java.util.List;

public class OrderProcessor {
    private static final double VAT_RATE = 0.21;
    private static final double DISCOUNT_THRESHOLD = 100.0;
    private static final double DISCOUNT_RATE = 0.10;
    
    private final List<LineItem> lineItems;
    
    public OrderProcessor(List<LineItem> lineItems) {
        validateAllLineItems(lineItems);
        this.lineItems = lineItems;
    }
    
    private void validateAllLineItems(List<LineItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one line item");
        }
        for (LineItem item : items) {
            item.validate();
        }
    }
    
    public String generateReceipt() {
        double subtotal = calculateSubtotal();
        double discountAmount = calculateDiscount(subtotal);
        double subtotalAfterDiscount = subtotal - discountAmount;
        double vatAmount = subtotalAfterDiscount * VAT_RATE;
        double total = subtotalAfterDiscount + vatAmount;
        
        return formatReceipt(subtotal, discountAmount, vatAmount, total);
    }
    
    private double calculateSubtotal() {
        return lineItems.stream()
            .mapToDouble(LineItem::getLineTotal)
            .sum();
    }
    
    private double calculateDiscount(double subtotal) {
        return subtotal > DISCOUNT_THRESHOLD ? subtotal * DISCOUNT_RATE : 0.0;
    }
    
    private String formatReceipt(double subtotal, double discount, double vat, double total) {
        StringBuilder receipt = new StringBuilder();
        receipt.append("════════════════════════════════════\n");
        receipt.append("           ORDER RECEIPT\n");
        receipt.append("════════════════════════════════════\n\n");
        
        for (LineItem item : lineItems) {
            receipt.append(String.format("%-25s %3d × €%7.2f = €%8.2f\n",
                item.getName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getLineTotal()
            ));
        }
        
        receipt.append("\n────────────────────────────────────\n");
        receipt.append(String.format("%-30s €%10.2f\n", "Subtotal:", subtotal));
        
        if (discount > 0) {
            receipt.append(String.format("%-30s €%10.2f\n", "Discount (10%):", -discount));
        }
        
        double subtotalAfterDiscount = subtotal - discount;
        receipt.append(String.format("%-30s €%10.2f\n", "VAT (21%):", vat));
        receipt.append("════════════════════════════════════\n");
        receipt.append(String.format("%-30s €%10.2f\n", "TOTAL:", total));
        receipt.append("════════════════════════════════════\n");
        
        return receipt.toString();
    }
}
