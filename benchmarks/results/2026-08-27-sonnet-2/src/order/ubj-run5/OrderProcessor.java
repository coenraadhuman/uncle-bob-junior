// OrderProcessor.java
package com.plg.order;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public final class OrderProcessor {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    private static final int EURO_SCALE = 2;

    public Receipt process(List<OrderLineItem> lineItems) {
        requireNonEmpty(lineItems);

        BigDecimal subtotal = roundToEuroCents(sumLineTotals(lineItems));
        BigDecimal discount = calculateDiscount(subtotal);
        BigDecimal discountedSubtotal = subtotal.subtract(discount);
        BigDecimal vat = roundToEuroCents(discountedSubtotal.multiply(VAT_RATE));
        BigDecimal total = discountedSubtotal.add(vat);

        return new Receipt(lineItems, subtotal, discount, vat, total);
    }

    private void requireNonEmpty(List<OrderLineItem> lineItems) {
        if (lineItems == null || lineItems.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one line item");
        }
    }

    private BigDecimal sumLineTotals(List<OrderLineItem> lineItems) {
        return lineItems.stream()
                .map(OrderLineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateDiscount(BigDecimal subtotal) {
        boolean exceedsThreshold = subtotal.compareTo(DISCOUNT_THRESHOLD) > 0;
        if (!exceedsThreshold) {
            return roundToEuroCents(BigDecimal.ZERO);
        }
        return roundToEuroCents(subtotal.multiply(DISCOUNT_RATE));
    }

    private BigDecimal roundToEuroCents(BigDecimal amount) {
        return amount.setScale(EURO_SCALE, RoundingMode.HALF_UP);
    }
}
