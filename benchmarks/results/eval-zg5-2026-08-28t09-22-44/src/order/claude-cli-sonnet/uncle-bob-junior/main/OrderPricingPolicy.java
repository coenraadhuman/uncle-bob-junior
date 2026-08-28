// File: OrderPricingPolicy.java
package com.postcodeloterij.orders;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public final class OrderPricingPolicy {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final int MONEY_SCALE = 2;

    public OrderTotals calculate(List<LineItem> lineItems) {
        BigDecimal subtotal = sumLineTotals(lineItems);
        BigDecimal discount = calculateDiscount(subtotal);
        BigDecimal discountedSubtotal = subtotal.subtract(discount);
        BigDecimal vatAmount = round(discountedSubtotal.multiply(VAT_RATE));
        BigDecimal grandTotal = round(discountedSubtotal.add(vatAmount));
        return new OrderTotals(round(subtotal), discount, vatAmount, grandTotal);
    }

    private BigDecimal sumLineTotals(List<LineItem> lineItems) {
        return lineItems.stream()
                .map(LineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateDiscount(BigDecimal subtotal) {
        boolean qualifiesForDiscount = subtotal.compareTo(DISCOUNT_THRESHOLD) > 0;
        if (!qualifiesForDiscount) {
            return round(BigDecimal.ZERO);
        }
        return round(subtotal.multiply(DISCOUNT_RATE));
    }

    private BigDecimal round(BigDecimal amount) {
        return amount.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
