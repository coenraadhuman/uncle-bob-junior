// OrderTotals.java
package com.plg.orders;

import java.math.BigDecimal;

public record OrderTotals(BigDecimal subtotalBeforeDiscount, BigDecimal discount, BigDecimal vat, BigDecimal total) {

    public BigDecimal subtotalAfterDiscount() {
        return subtotalBeforeDiscount.subtract(discount);
    }
}
