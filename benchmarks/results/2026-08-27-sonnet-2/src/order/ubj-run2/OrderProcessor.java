package com.example.orders;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Computes an order's totals: 10% discount on pre-VAT subtotals over
 * {@link #DISCOUNT_THRESHOLD}, then 21% VAT on the discounted subtotal.
 */
public final class OrderProcessor {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final int MONEY_SCALE = 2;

    public Receipt process(Order order) {
        BigDecimal subtotal = sumLineItems(order.lineItems());
        BigDecimal discount = calculateDiscount(subtotal);
        BigDecimal discountedSubtotal = subtotal.subtract(discount);
        BigDecimal vat = discountedSubtotal.multiply(VAT_RATE);
        BigDecimal total = discountedSubtotal.add(vat);

        return new Receipt(
                order.lineItems(),
                round(subtotal),
                round(discount),
                round(vat),
                round(total));
    }

    private BigDecimal sumLineItems(List<LineItem> lineItems) {
        return lineItems.stream()
                .map(LineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateDiscount(BigDecimal subtotal) {
        if (subtotal.compareTo(DISCOUNT_THRESHOLD) <= 0) {
            return BigDecimal.ZERO;
        }
        return subtotal.multiply(DISCOUNT_RATE);
    }

    private BigDecimal round(BigDecimal amount) {
        return amount.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
