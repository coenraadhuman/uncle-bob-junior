import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    
    private List<LineItem> items;
    
    public Order() {
        this.items = new ArrayList<>();
    }
    
    public void addLineItem(String description, BigDecimal quantity, BigDecimal unitPrice) 
            throws IllegalArgumentException {
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be empty");
        }
        if (quantity == null || quantity.signum() < 0) {
            throw new IllegalArgumentException("Quantity must be non-negative");
        }
        if (unitPrice == null || unitPrice.signum() < 0) {
            throw new IllegalArgumentException("Unit price must be non-negative");
        }
        items.add(new LineItem(description, quantity, unitPrice));
    }
    
    public String generateReceipt() {
        StringBuilder receipt = new StringBuilder();
        receipt.append("=== RECEIPT ===\n");
        
        BigDecimal subtotal = BigDecimal.ZERO;
        
        for (LineItem item : items) {
            BigDecimal lineTotal = item.getTotal();
            subtotal = subtotal.add(lineTotal);
            receipt.append(String.format("%s x %.2f @ €%.2f = €%.2f\n",
                item.description, item.quantity, item.unitPrice, lineTotal));
        }
        
        receipt.append("\nSubtotal: €").append(formatCurrency(subtotal));
        
        BigDecimal discount = BigDecimal.ZERO;
        if (subtotal.compareTo(DISCOUNT_THRESHOLD) > 0) {
            discount = subtotal.multiply(DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP);
            receipt.append("\nDiscount (10%): -€").append(formatCurrency(discount));
        }
        
        BigDecimal discountedSubtotal = subtotal.subtract(discount);
        BigDecimal vat = discountedSubtotal.multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = discountedSubtotal.add(vat);
        
        receipt.append("\nVAT (21%): €").append(formatCurrency(vat));
        receipt.append("\nTotal: €").append(formatCurrency(total));
        
        return receipt.toString();
    }
    
    private String formatCurrency(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP).toString();
    }
    
    private static class LineItem {
        String description;
        BigDecimal quantity;
        BigDecimal unitPrice;
        
        LineItem(String description, BigDecimal quantity, BigDecimal unitPrice) {
            this.description = description;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }
        
        BigDecimal getTotal() {
            return quantity.multiply(unitPrice).setScale(2, RoundingMode.HALF_UP);
        }
    }
    
    public static void main(String[] args) {
        Order order = new Order();
        order.addLineItem("Widget A", new BigDecimal("2"), new BigDecimal("45.00"));
        order.addLineItem("Widget B", new BigDecimal("3"), new BigDecimal("20.00"));
        
        System.out.println(order.generateReceipt());
    }
}
