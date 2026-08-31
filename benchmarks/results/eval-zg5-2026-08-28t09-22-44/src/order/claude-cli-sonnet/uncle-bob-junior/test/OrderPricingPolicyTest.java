// File: OrderPricingPolicyTest.java
package com.postcodeloterij.orders;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderPricingPolicyTest {

    private final OrderPricingPolicy pricingPolicy = new OrderPricingPolicy();

    @Test
    void appliesNoDiscountBelowThreshold() {
        List<LineItem> items = List.of(new LineItem("Widget", 2, new BigDecimal("30.00")));

        OrderTotals totals = pricingPolicy.calculate(items);

        assertEquals(new BigDecimal("60.00"), totals.preDiscountSubtotal());
        assertEquals(new BigDecimal("0.00"), totals.discountAmount());
        assertEquals(new BigDecimal("12.60"), totals.vatAmount());
        assertEquals(new BigDecimal("72.60"), totals.grandTotal());
    }

    @Test
    void appliesNoDiscountAtExactThreshold() {
        List<LineItem> items = List.of(new LineItem("Widget", 2, new BigDecimal("50.00")));

        OrderTotals totals = pricingPolicy.calculate(items);

        assertEquals(new BigDecimal("0.00"), totals.discountAmount());
        assertEquals(new BigDecimal("121.00"), totals.grandTotal());
    }

    @Test
    void appliesDiscountAboveThreshold() {
        List<LineItem> items = List.of(new LineItem("Widget", 3, new BigDecimal("50.00")));

        OrderTotals totals = pricingPolicy.calculate(items);

        assertEquals(new BigDecimal("150.00"), totals.preDiscountSubtotal());
        assertEquals(new BigDecimal("15.00"), totals.discountAmount());
        assertEquals(new BigDecimal("28.35"), totals.vatAmount());
        assertEquals(new BigDecimal("163.35"), totals.grandTotal());
    }
}
