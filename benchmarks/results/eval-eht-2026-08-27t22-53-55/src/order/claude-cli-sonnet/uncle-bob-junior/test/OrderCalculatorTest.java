package com.plg.orders;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class OrderCalculatorTest {

    private final OrderCalculator calculator = new OrderCalculator();

    @Test
    void appliesNoDiscountWhenSubtotalAtThreshold() {
        LineItem item = new LineItem("Widget", new BigDecimal("100.00"), 1);
        OrderTotals totals = calculator.calculate(List.of(item));

        assertEquals(new BigDecimal("0.00"), totals.discountAmount());
        assertEquals(new BigDecimal("121.00"), totals.total());
    }

    @Test
    void appliesDiscountWhenSubtotalExceedsThreshold() {
        LineItem item = new LineItem("Widget", new BigDecimal("50.00"), 3);
        OrderTotals totals = calculator.calculate(List.of(item));

        assertEquals(new BigDecimal("15.00"), totals.discountAmount());
        assertEquals(new BigDecimal("163.35"), totals.total());
    }

    @Test
    void rejectsEmptyOrder() {
        assertThrows(IllegalArgumentException.class, () -> calculator.calculate(List.of()));
    }

    @Test
    void rejectsNullOrder() {
        assertThrows(IllegalArgumentException.class, () -> calculator.calculate(null));
    }
}
