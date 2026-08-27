// OrderCalculator.java
package com.plg.orders;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public final class OrderCalculator {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final int MONEY_SCALE = 2;

    private OrderCalculator() {
    }

    public static OrderTotals calculateTotals(List<LineItem> lineItems) {
        if (lineItems == null || lineItems.isEmpty()) {
            throw new IllegalArgumentException("An order must contain at least one line item");
        }

        BigDecimal subtotal = round(sumLineTotals(lineItems));
        BigDecimal discount = round(calculateDiscount(subtotal));
        BigDecimal subtotalAfterDiscount = subtotal.subtract(discount);
        BigDecimal vat = round(subtotalAfterDiscount.multiply(VAT_RATE));
        BigDecimal total = subtotalAfterDiscount.add(vat);

        return new OrderTotals(subtotal, discount, vat, total);
    }

    private static BigDecimal sumLineTotals(List<LineItem> lineItems) {
        return lineItems.stream()
                .map(LineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal calculateDiscount(BigDecimal subtotal) {
        return isEligibleForDiscount(subtotal) ? subtotal.multiply(DISCOUNT_RATE) : BigDecimal.ZERO;
    }

    private static boolean isEligibleForDiscount(BigDecimal subtotal) {
        return subtotal.compareTo(DISCOUNT_THRESHOLD) > 0;
    }

    private static BigDecimal round(BigDecimal amount) {
        return amount.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
