// OrderPricing.java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public final class OrderPricing {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final int EURO_SCALE = 2;

    private final BigDecimal subtotal;
    private final BigDecimal discount;
    private final BigDecimal vat;
    private final BigDecimal total;

    private OrderPricing(BigDecimal subtotal, BigDecimal discount, BigDecimal vat, BigDecimal total) {
        this.subtotal = subtotal;
        this.discount = discount;
        this.vat = vat;
        this.total = total;
    }

    static OrderPricing of(List<OrderItem> items) {
        BigDecimal subtotal = sumLineTotals(items);
        BigDecimal discount = discountFor(subtotal);
        BigDecimal discountedSubtotal = subtotal.subtract(discount);
        BigDecimal vat = vatFor(discountedSubtotal);
        BigDecimal total = round(discountedSubtotal.add(vat));
        return new OrderPricing(round(subtotal), round(discount), round(vat), total);
    }

    private static BigDecimal sumLineTotals(List<OrderItem> items) {
        return items.stream()
                .map(OrderItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal discountFor(BigDecimal subtotal) {
        boolean qualifiesForDiscount = subtotal.compareTo(DISCOUNT_THRESHOLD) > 0;
        return qualifiesForDiscount ? subtotal.multiply(DISCOUNT_RATE) : BigDecimal.ZERO;
    }

    private static BigDecimal vatFor(BigDecimal amount) {
        return amount.multiply(VAT_RATE);
    }

    private static BigDecimal round(BigDecimal amount) {
        return amount.setScale(EURO_SCALE, RoundingMode.HALF_UP);
    }

    BigDecimal subtotal() {
        return subtotal;
    }

    BigDecimal discount() {
        return discount;
    }

    BigDecimal vat() {
        return vat;
    }

    BigDecimal total() {
        return total;
    }
}
