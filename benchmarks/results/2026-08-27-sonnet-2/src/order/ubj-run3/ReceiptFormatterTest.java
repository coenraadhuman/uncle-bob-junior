package com.example.orders;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReceiptFormatterTest {

    private final ReceiptFormatter formatter = new ReceiptFormatter();

    @Test
    void includesDiscountLineWhenDiscountApplied() {
        OrderTotals totals = new OrderTotals(
                new BigDecimal("120.00"), new BigDecimal("12.00"),
                new BigDecimal("22.68"), new BigDecimal("130.68"));
        List<LineItem> items = List.of(new LineItem("Widget", new BigDecimal("60.00"), 2));

        String receipt = formatter.format(items, totals);

        assertTrue(receipt.contains("Discount (10%)"));
        assertTrue(receipt.contains("EUR 130.68"));
    }

    @Test
    void omitsDiscountLineWhenNoDiscountApplied() {
        OrderTotals totals = new OrderTotals(
                new BigDecimal("50.00"), new BigDecimal("0.00"),
                new BigDecimal("10.50"), new BigDecimal("60.50"));
        List<LineItem> items = List.of(new LineItem("Widget", new BigDecimal("50.00"), 1));

        String receipt = formatter.format(items, totals);

        assertFalse(receipt.contains("Discount"));
        assertTrue(receipt.contains("EUR 60.50"));
    }
}
