import java.math.BigDecimal;

class Receipt {
    private final Order order;
    private final BigDecimal subtotal;
    private final BigDecimal discountAmount;
    private final BigDecimal vatAmount;
    private final BigDecimal total;
    
    Receipt(Order order, BigDecimal subtotal, BigDecimal discountAmount, 
            BigDecimal vatAmount, BigDecimal total) {
        this.order = order;
        this.subtotal = subtotal;
        this.discountAmount = discountAmount;
        this.vatAmount = vatAmount;
        this.total = total;
    }
    
    String format() {
        StringBuilder receipt = new StringBuilder();
        receipt.append("=== RECEIPT ===\n");
        appendLineItems(receipt);
        appendSummary(receipt);
        return receipt.toString();
    }
    
    private void appendLineItems(StringBuilder receipt) {
        for (LineItem item : order.items()) {
            receipt.append(String.format("%-30s %3d x €%7.2f = €%8.2f\n",
                item.description(), item.quantity(), item.unitPrice(), item.lineTotal()));
        }
    }
    
    private void appendSummary(StringBuilder receipt) {
        receipt.append(String.format("%-40s €%8.2f\n", "Subtotal:", subtotal));
        
        if (discountAmount.compareTo(BigDecimal.ZERO) > 0) {
            receipt.append(String.format("%-40s -€%7.2f\n", "Discount (10%):", discountAmount));
        }
        
        BigDecimal afterDiscount = subtotal.subtract(discountAmount);
        receipt.append(String.format("%-40s €%8.2f\n", "After discount:", afterDiscount));
        receipt.append(String.format("%-40s €%8.2f\n", "VAT (21%):", vatAmount));
        receipt.append("==========================================\n");
        receipt.append(String.format("%-40s €%8.2f\n", "TOTAL:", total));
    }
}
