import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

class LineItem {
    private final String description;
    private final int quantity;
    private final BigDecimal unitPrice;
    
    LineItem(String description, int quantity, BigDecimal unitPrice) {
        this.description = Objects.requireNonNull(description, "Description cannot be null");
        this.quantity = quantity;
        this.unitPrice = Objects.requireNonNull(unitPrice, "Unit price cannot be null");
        validateLineItem();
    }
    
    private void validateLineItem() {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        if (unitPrice.signum() <= 0) throw new IllegalArgumentException("Unit price must be positive");
    }
    
    BigDecimal subtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
    
    String description() { return description; }
    int quantity() { return quantity; }
    BigDecimal unitPrice() { return unitPrice; }
}

class Order {
    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    
    private final List<LineItem> items;
    
    Order(List<LineItem> items) {
        this.items = Objects.requireNonNull(items, "Items list cannot be null");
        if (items.isEmpty()) throw new IllegalArgumentException("Order must have at least one item");
    }
    
    BigDecimal preVatTotal() {
        return items.stream()
            .map(LineItem::subtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    BigDecimal discountAmount() {
        BigDecimal total = preVatTotal();
        return total.compareTo(DISCOUNT_THRESHOLD) > 0
            ? total.multiply(DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
    }
    
    BigDecimal totalAfterDiscount() {
        return preVatTotal().subtract(discountAmount());
    }
    
    BigDecimal vatAmount() {
        return totalAfterDiscount().multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);
    }
    
    BigDecimal grandTotal() {
        return totalAfterDiscount().add(vatAmount());
    }
    
    String receipt() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ORDER RECEIPT ===\n");
        items.forEach(item -> 
            sb.append(String.format("  %s x%d @ €%.2f = €%.2f\n",
                item.description(), item.quantity(), item.unitPrice(), item.subtotal()))
        );
        sb.append(String.format("Subtotal:        €%.2f\n", preVatTotal()));
        if (discountAmount().signum() > 0) {
            sb.append(String.format("Discount (10%%):  -€%.2f\n", discountAmount()));
            sb.append(String.format("After discount:  €%.2f\n", totalAfterDiscount()));
        }
        sb.append(String.format("VAT (21%%):       €%.2f\n", vatAmount()));
        sb.append(String.format("TOTAL:           €%.2f\n", grandTotal()));
        return sb.toString();
    }
}
