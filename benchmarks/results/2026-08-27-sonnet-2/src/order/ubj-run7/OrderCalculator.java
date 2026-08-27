// filename: OrderCalculator.java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

class OrderCalculator {

    OrderTotals calculate(List<LineItem> lineItems) {
        requireNonEmpty(lineItems);
        BigDecimal subtotal = sumLineTotals(lineItems);
        BigDecimal discount = discountFor(subtotal);
        BigDecimal vat = vatFor(subtotal.subtract(discount));
        BigDecimal total = subtotal.subtract(discount).add(vat);
        return new OrderTotals(round(subtotal), round(discount), round(vat), round(total));
    }

    private void requireNonEmpty(List<LineItem> lineItems) {
        if (lineItems == null || lineItems.isEmpty()) {
            throw new InvalidOrderException("An order must contain at least one line item");
        }
    }

    private BigDecimal sumLineTotals(List<LineItem> lineItems) {
        return lineItems.stream()
                .map(LineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal discountFor(BigDecimal subtotal) {
        if (subtotal.compareTo(PricingRules.DISCOUNT_THRESHOLD) <= 0) {
            return BigDecimal.ZERO;
        }
        return subtotal.multiply(PricingRules.DISCOUNT_RATE);
    }

    private BigDecimal vatFor(BigDecimal amount) {
        return amount.multiply(PricingRules.VAT_RATE);
    }

    private BigDecimal round(BigDecimal amount) {
        return amount.setScale(PricingRules.MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
