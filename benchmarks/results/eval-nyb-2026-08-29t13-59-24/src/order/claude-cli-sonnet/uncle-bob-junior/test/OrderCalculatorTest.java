// OrderCalculatorTest.java
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderCalculatorTest {

    private final OrderCalculator calculator = new OrderCalculator();

    @Test
    void appliesNoDiscountAtOrBelowThreshold() {
        LineItem item = new LineItem("Ticket", new BigDecimal("100.00"), 1, new BigDecimal("100.00"));

        OrderTotals totals = calculator.calculate(List.of(item));

        assertEquals(new BigDecimal("0.00"), totals.discountAmount());
        assertEquals(new BigDecimal("121.00"), totals.total());
    }

    @Test
    void appliesDiscountWhenSubtotalExceedsThreshold() {
        LineItem item = new LineItem("Ticket", new BigDecimal("200.00"), 1, new BigDecimal("200.00"));

        OrderTotals totals = calculator.calculate(List.of(item));

        assertEquals(new BigDecimal("20.00"), totals.discountAmount());
    }

    @Test
    void computesVatOnDiscountedSubtotal() {
        LineItem item = new LineItem("Ticket", new BigDecimal("200.00"), 1, new BigDecimal("200.00"));

        OrderTotals totals = calculator.calculate(List.of(item));

        assertEquals(new BigDecimal("37.80"), totals.vatAmount());
        assertEquals(new BigDecimal("217.80"), totals.total());
    }
}
