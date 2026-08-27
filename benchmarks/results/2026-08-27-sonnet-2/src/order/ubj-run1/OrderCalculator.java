// OrderCalculator.java
package com.example.order;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public final class OrderCalculator {

    static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final int MONEY_SCALE = 2;

    private OrderCalculator() {
    }

    public static OrderTotals calculate(List<LineItem> lineItems) {
        BigDecimal subtotal = subtotalOf(lineItems);
        BigDecimal discount = discountOn(subtotal);
        BigDecimal discountedSubtotal = subtotal.subtract(discount);
        BigDecimal vat = vatOn(discountedSubtotal);
        BigDecimal total = discountedSubtotal.add(vat);
        return new OrderTotals(round(subtotal), round(discount), round(vat), round(total));
    }

    private static BigDecimal subtotalOf(List<LineItem> lineItems) {
        return lineItems.stream()
                .map(LineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal discountOn(BigDecimal subtotal) {
        boolean qualifiesForDiscount = subtotal.compareTo(DISCOUNT_THRESHOLD) > 0;
        return qualifiesForDiscount ? subtotal.multiply(DISCOUNT_RATE) : BigDecimal.ZERO;
    }

    private static BigDecimal vatOn(BigDecimal amount) {
        return amount.multiply(VAT_RATE);
    }

    private static BigDecimal round(BigDecimal amount) {
        return amount.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
