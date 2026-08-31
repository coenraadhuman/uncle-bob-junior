// File: OrderTotals.java
package com.postcodeloterij.orders;

import java.math.BigDecimal;

/**
 * Pricing breakdown for an order, all fields in EUR rounded to 2 decimals.
 * discountAmount is always non-negative; it is the amount subtracted, not a negative delta.
 */
public record OrderTotals(BigDecimal preDiscountSubtotal,
                           BigDecimal discountAmount,
                           BigDecimal vatAmount,
                           BigDecimal grandTotal) {
}
