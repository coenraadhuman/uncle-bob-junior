package com.plg.orders;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ReceiptFormatterTest {

    private final ReceiptFormatter formatter = new ReceiptFormatter();

    @Test
    void includesDiscountLineWhenDiscountApplied() {
        LineItem item = new LineItem("Widget", new BigDecimal("50.00"), 3);
        OrderTotals totals = new OrderCalculator().calculate(List.of(item));

        String receipt = formatter.format(List.of(item), totals);

        assertTrue(receipt.contains("Discount: -15.00 EUR"));
        assertTrue(receipt.contains("Total: 163.35 EUR"));
    }

    @Test
    void omitsDiscountLineWhenNoDiscountApplied() {
        LineItem item = new LineItem("Widget", new BigDecimal("10.00"), 1);
        OrderTotals totals = new OrderCalculator().calculate(List.of(item));

        String receipt = formatter.format(List.of(item), totals);

        assertFalse(receipt.contains("Discount"));
    }
}
