package com.plg.orders;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public final class OrderCalculator {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final int MONEY_SCALE = 2;

    public OrderTotals calculate(List<LineItem> lineItems) {
        if (lineItems == null || lineItems.isEmpty()) {
            throw new IllegalArgumentException("an order must contain at least one line item");
        }

        BigDecimal subtotal = sumLineTotals(lineItems);
        BigDecimal discount = calculateDiscount(subtotal);
        BigDecimal subtotalAfterDiscount = round(subtotal.subtract(discount));
        BigDecimal vat = round(subtotalAfterDiscount.multiply(VAT_RATE));
        BigDecimal total = round(subtotalAfterDiscount.add(vat));

        return new OrderTotals(round(subtotal), discount, subtotalAfterDiscount, vat, total);
    }

    private BigDecimal sumLineTotals(List<LineItem> lineItems) {
        return lineItems.stream()
                .map(LineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateDiscount(BigDecimal subtotal) {
        boolean qualifiesForDiscount = subtotal.compareTo(DISCOUNT_THRESHOLD) > 0;
        if (!qualifiesForDiscount) {
            return BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        }
        return round(subtotal.multiply(DISCOUNT_RATE));
    }

    private BigDecimal round(BigDecimal amount) {
        return amount.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
