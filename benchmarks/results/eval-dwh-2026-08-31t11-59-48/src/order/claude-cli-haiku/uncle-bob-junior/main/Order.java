import java.util.List;

public class Order {
    private static final double VAT_RATE = 0.21;
    private static final double DISCOUNT_RATE = 0.10;
    private static final double DISCOUNT_THRESHOLD = 100.00;
    
    private final List<LineItem> lineItems;
    
    public Order(List<LineItem> lineItems) {
        validateLineItems(lineItems);
        this.lineItems = List.copyOf(lineItems);
    }
    
    private void validateLineItems(List<LineItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Line items cannot be null or empty");
        }
        items.forEach(this::validateLineItem);
    }
    
    private void validateLineItem(LineItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Line item cannot be null");
        }
        if (item.quantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (item.unitPrice() < 0) {
            throw new IllegalArgumentException("Unit price cannot be negative");
        }
    }
    
    public String generateReceipt() {
        double subtotal = calculateSubtotal();
        double discount = calculateDiscount(subtotal);
        double afterDiscount = subtotal - discount;
        double vat = calculateVat(afterDiscount);
        double total = afterDiscount + vat;
        
        return buildReceipt(subtotal, discount, afterDiscount, vat, total);
    }
    
    private double calculateSubtotal() {
        return lineItems.stream()
            .mapToDouble(item -> item.quantity() * item.unitPrice())
            .sum();
    }
    
    private double calculateDiscount(double subtotal) {
        return subtotal > DISCOUNT_THRESHOLD ? subtotal * DISCOUNT_RATE : 0.0;
    }
    
    private double calculateVat(double amountAfterDiscount) {
        return amountAfterDiscount * VAT_RATE;
    }
    
    private String buildReceipt(double subtotal, double discount, 
                                 double afterDiscount, double vat, double total) {
        StringBuilder receipt = new StringBuilder();
        receipt.append("=== ORDER RECEIPT ===\n");
        appendLineItems(receipt);
        receipt.append(String.format("\nSubtotal: €%.2f\n", subtotal));
        if (discount > 0.0) {
            receipt.append(String.format("Discount (10%%): -€%.2f\n", discount));
        }
        receipt.append(String.format("Amount after discount: €%.2f\n", afterDiscount));
        receipt.append(String.format("VAT (21%%): €%.2f\n", vat));
        receipt.append(String.format("TOTAL: €%.2f\n", total));
        return receipt.toString();
    }
    
    private void appendLineItems(StringBuilder receipt) {
        for (LineItem item : lineItems) {
            double lineTotal = item.quantity() * item.unitPrice();
            receipt.append(String.format("%s x%d @ €%.2f = €%.2f\n",
                item.name(), item.quantity(), item.unitPrice(), lineTotal));
        }
    }
}
