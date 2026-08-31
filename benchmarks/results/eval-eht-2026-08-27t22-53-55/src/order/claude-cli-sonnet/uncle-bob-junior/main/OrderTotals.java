package com.plg.orders;

import java.math.BigDecimal;

public record OrderTotals(
        BigDecimal subtotalBeforeDiscount,
        BigDecimal discountAmount,
        BigDecimal subtotalAfterDiscount,
        BigDecimal vatAmount,
        BigDecimal total) {
}
