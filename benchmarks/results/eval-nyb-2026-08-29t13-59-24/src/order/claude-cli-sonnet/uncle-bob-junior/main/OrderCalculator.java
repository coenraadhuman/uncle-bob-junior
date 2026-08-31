// OrderCalculator.java
import java.math.BigDecimal;
import java.util.List;

public final class OrderCalculator {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");

    public OrderTotals calculate(List<LineItem> items) {
        BigDecimal subtotal = sumLineTotals(items);
        BigDecimal discountAmount = discountFor(subtotal);
        BigDecimal discountedSubtotal = subtotal.subtract(discountAmount);
        BigDecimal vatAmount = vatFor(discountedSubtotal);
        BigDecimal total = discountedSubtotal.add(vatAmount);
        return new OrderTotals(subtotal, discountAmount, vatAmount, total);
    }

    private BigDecimal sumLineTotals(List<LineItem> items) {
        return items.stream()
                .map(LineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(MoneyFormat.SCALE, MoneyFormat.ROUNDING);
    }

    private BigDecimal discountFor(BigDecimal subtotal) {
        if (subtotal.compareTo(DISCOUNT_THRESHOLD) <= 0) {
            return BigDecimal.ZERO.setScale(MoneyFormat.SCALE, MoneyFormat.ROUNDING);
        }
        return subtotal.multiply(DISCOUNT_RATE).setScale(MoneyFormat.SCALE, MoneyFormat.ROUNDING);
    }

    private BigDecimal vatFor(BigDecimal discountedSubtotal) {
        return discountedSubtotal.multiply(VAT_RATE).setScale(MoneyFormat.SCALE, MoneyFormat.ROUNDING);
    }
}
