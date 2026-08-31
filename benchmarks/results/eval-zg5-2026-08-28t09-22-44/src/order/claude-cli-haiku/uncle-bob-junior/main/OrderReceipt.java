import java.math.BigDecimal;
import java.util.List;

public class OrderReceipt {
    private final List<LineItem> items;
    private final BigDecimal subtotal;
    private final BigDecimal discount;
    private final BigDecimal vatAmount;
    private final BigDecimal total;
    
    public OrderReceipt(List<LineItem> items, BigDecimal subtotal, BigDecimal discount, 
                       BigDecimal vatAmount, BigDecimal total) {
        this.items = items;
        this.subtotal = subtotal;
        this.discount = discount;
        this.vatAmount = vatAmount;
        this.total = total;
    }
    
    public BigDecimal getSubtotal() {
        return subtotal;
    }
    
    public BigDecimal getDiscount() {
        return discount;
    }
    
    public BigDecimal getVatAmount() {
        return vatAmount;
    }
    
    public BigDecimal getTotal() {
        return total;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ORDER RECEIPT ===\n");
        
        for (LineItem item : items) {
            sb.append(String.format("%s (qty: %d x €%.2f) = €%.2f\n",
                item.getDescription(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.lineTotal()));
        }
        
        sb.append("\n");
        sb.append(String.format("Subtotal:        €%.2f\n", subtotal));
        
        if (discount.compareTo(BigDecimal.ZERO) > 0) {
            sb.append(String.format("Discount (10%%):  -€%.2f\n", discount));
        }
        
        sb.append(String.format("VAT (21%%):       €%.2f\n", vatAmount));
        sb.append(String.format("TOTAL:           €%.2f\n", total));
        
        return sb.toString();
    }
}
