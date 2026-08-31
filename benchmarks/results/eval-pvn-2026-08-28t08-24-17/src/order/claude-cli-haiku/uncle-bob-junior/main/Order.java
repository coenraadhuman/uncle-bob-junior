import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class Order {
    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100");
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    private final List<LineItem> lineItems;

    public Order(List<LineItem> lineItems) {
        Objects.requireNonNull(lineItems, "lineItems cannot be null");
        if (lineItems.isEmpty()) {
            throw new IllegalArgumentException("order must have at least one line item");
        }
        this.lineItems = Collections.unmodifiableList(new ArrayList<>(lineItems));
    }

    public Receipt generateReceipt() {
        BigDecimal subtotal = calculateSubtotal();
        BigDecimal discount = calculateDiscount(subtotal);
        BigDecimal discountedTotal = subtotal.subtract(discount);
        BigDecimal vat = calculateVat(discountedTotal);
        BigDecimal total = discountedTotal.add(vat);

        OrderCalculation calculation = new OrderCalculation(subtotal, discount, discountedTotal, vat, total);
        return new Receipt(lineItems, calculation);
    }

    private BigDecimal calculateSubtotal() {
        return lineItems.stream()
            .map(LineItem::subtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateDiscount(BigDecimal subtotal) {
        if (subtotal.compareTo(DISCOUNT_THRESHOLD) > 0) {
            return subtotal.multiply(DISCOUNT_RATE).setScale(2, ROUNDING);
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal calculateVat(BigDecimal discountedTotal) {
        return discountedTotal.multiply(VAT_RATE).setScale(2, ROUNDING);
    }
}
