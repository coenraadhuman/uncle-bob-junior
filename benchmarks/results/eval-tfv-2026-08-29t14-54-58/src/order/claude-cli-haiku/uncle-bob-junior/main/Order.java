import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class Order {
    private static final BigDecimal VAT_RATE = BigDecimal.valueOf(0.21);
    private static final BigDecimal DISCOUNT_RATE = BigDecimal.valueOf(0.10);
    private static final BigDecimal DISCOUNT_THRESHOLD = BigDecimal.valueOf(100);

    private final List<LineItem> items;
    private final BigDecimal subtotal;
    private final BigDecimal discountAmount;
    private final BigDecimal discountedSubtotal;
    private final BigDecimal vat;
    private final BigDecimal total;

    Order(List<LineItem> items) {
        this.items = new ArrayList<>(Objects.requireNonNull(items));
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one item");
        }
        this.subtotal = computeSubtotal();
        this.discountAmount = computeDiscount();
        this.discountedSubtotal = subtotal.subtract(discountAmount);
        this.vat = discountedSubtotal.multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);
        this.total = discountedSubtotal.add(vat).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal computeSubtotal() {
        BigDecimal sum = BigDecimal.ZERO;
        for (LineItem item : items) {
            sum = sum.add(item.subtotal());
        }
        return sum.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal computeDiscount() {
        if (subtotal.compareTo(DISCOUNT_THRESHOLD) > 0) {
            return subtotal.multiply(DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    String receipt() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ORDER RECEIPT ===\n");
        for (LineItem item : items) {
            sb.append(String.format("%s x%d: €%.2f\n", 
                item.description(), item.quantity(), item.subtotal()));
        }
        sb.append(String.format("\nSubtotal: €%.2f\n", subtotal));
        if (discountAmount.signum() > 0) {
            sb.append(String.format("Discount (10%%): -€%.2f\n", discountAmount));
            sb.append(String.format("After Discount: €%.2f\n", discountedSubtotal));
        }
        sb.append(String.format("VAT (21%%): €%.2f\n", vat));
        sb.append(String.format("Total: €%.2f\n", total));
        return sb.toString();
    }

    BigDecimal getTotal() { return total; }
    BigDecimal getSubtotal() { return subtotal; }
    BigDecimal getDiscount() { return discountAmount; }
    BigDecimal getVat() { return vat; }
}
