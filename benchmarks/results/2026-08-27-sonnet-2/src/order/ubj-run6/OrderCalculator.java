package com.plg.orders;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * All amounts are EUR, rounded to 2 decimals with HALF_UP at each monetary step
 * to match standard invoice rounding conventions.
 */
final class OrderCalculator {

    static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final int MONEY_SCALE = 2;

    OrderTotals calculate(List<LineItem> lineItems) {
        BigDecimal subtotal = sumLineItems(lineItems);
        BigDecimal discount = calculateDiscount(subtotal);
        BigDecimal discountedSubtotal = subtotal.subtract(discount);
        BigDecimal vat = round(discountedSubtotal.multiply(VAT_RATE));
        BigDecimal total = discountedSubtotal.add(vat);
        return new OrderTotals(subtotal, discount, vat, total);
    }

    private BigDecimal sumLineItems(List<LineItem> lineItems) {
        return lineItems.stream()
                .map(this::lineTotal)
                .reduce(BigDecimal.ZERO.setScale(MONEY_SCALE), BigDecimal::add);
    }

    private BigDecimal lineTotal(LineItem item) {
        return round(item.unitPrice().multiply(BigDecimal.valueOf(item.quantity())));
    }

    private BigDecimal calculateDiscount(BigDecimal subtotal) {
        boolean eligible = subtotal.compareTo(DISCOUNT_THRESHOLD) > 0;
        return eligible ? round(subtotal.multiply(DISCOUNT_RATE)) : BigDecimal.ZERO.setScale(MONEY_SCALE);
    }

    private BigDecimal round(BigDecimal value) {
        return value.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
