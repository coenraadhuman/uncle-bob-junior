import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class LineItem {
    private final int quantity;
    private final BigDecimal unitPrice;
    
    public LineItem(int quantity, BigDecimal unitPrice) {
        validateQuantity(quantity);
        validatePrice(unitPrice);
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }
    
    private void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
    }
    
    private void validatePrice(BigDecimal price) {
        if (price == null || price.signum() < 0) {
            throw new IllegalArgumentException("Price cannot be null or negative");
        }
    }
    
    public BigDecimal total() {
        return unitPrice.multiply(new BigDecimal(quantity));
    }
}

public class Order {
    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;
    
    private final List<LineItem> items;
    
    public Order() {
        this.items = new ArrayList<>();
    }
    
    public void addItem(LineItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Item cannot be null");
        }
        items.add(item);
    }
    
    public BigDecimal subtotal() {
        return items.stream()
            .map(LineItem::total)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public BigDecimal discountAmount() {
        BigDecimal sub = subtotal();
        if (sub.compareTo(DISCOUNT_THRESHOLD) > 0) {
            return sub.multiply(DISCOUNT_RATE).setScale(SCALE, ROUNDING);
        }
        return BigDecimal.ZERO;
    }
    
    public BigDecimal amountAfterDiscount() {
        return subtotal().subtract(discountAmount()).setScale(SCALE, ROUNDING);
    }
    
    public BigDecimal vat() {
        return amountAfterDiscount().multiply(VAT_RATE).setScale(SCALE, ROUNDING);
    }
    
    public BigDecimal total() {
        return amountAfterDiscount().add(vat()).setScale(SCALE, ROUNDING);
    }
    
    public String receipt() {
        StringBuilder sb = new StringBuilder();
        sb.append("RECEIPT\n");
        sb.append("=======\n");
        appendLineItems(sb);
        sb.append("-------\n");
        sb.append(String.format("Subtotal: €%.2f\n", subtotal()));
        appendDiscountIfApplied(sb);
        sb.append(String.format("VAT (21%%): €%.2f\n", vat()));
        sb.append("=======\n");
        sb.append(String.format("Total: €%.2f\n", total()));
        return sb.toString();
    }
    
    private void appendLineItems(StringBuilder sb) {
        for (LineItem item : items) {
            sb.append(String.format("Item: €%.2f\n", item.total()));
        }
    }
    
    private void appendDiscountIfApplied(StringBuilder sb) {
        if (discountAmount().signum() > 0) {
            sb.append(String.format("Discount (10%%): -€%.2f\n", discountAmount()));
        }
    }
}
