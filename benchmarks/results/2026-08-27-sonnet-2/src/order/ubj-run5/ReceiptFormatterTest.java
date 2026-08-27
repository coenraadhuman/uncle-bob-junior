// ReceiptFormatterTest.java
package com.plg.order;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReceiptFormatterTest {

    private final ReceiptFormatter formatter = new ReceiptFormatter();

    @Test
    void includesDiscountLineOnlyWhenDiscountApplied() {
        Receipt withDiscount = new Receipt(
                List.of(new OrderLineItem("Widget", new BigDecimal("150.00"), 1)),
                new BigDecimal("150.00"), new BigDecimal("15.00"),
                new BigDecimal("28.35"), new BigDecimal("163.35"));

        Receipt withoutDiscount = new Receipt(
                List.of(new OrderLineItem("Widget", new BigDecimal("50.00"), 1)),
                new BigDecimal("50.00"), new BigDecimal("0.00"),
                new BigDecimal("10.50"), new BigDecimal("60.50"));

        assertTrue(formatter.format(withDiscount).contains("Discount"));
        assertFalse(formatter.format(withoutDiscount).contains("Discount"));
    }
}
