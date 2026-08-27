// filename: OrderCalculatorTest.java
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderCalculatorTest {

    private final OrderCalculator calculator = new OrderCalculator();

    @Test
    void appliesNoDiscountBelowThreshold() {
        List<LineItem> lineItems = List.of(new LineItem("Widget", 2, new BigDecimal("10.00")));

        OrderTotals totals = calculator.calculate(lineItems);

        assertEquals(new BigDecimal("20.00"), totals.subtotal());
        assertEquals(new BigDecimal("0.00"), totals.discount());
        assertEquals(new BigDecimal("4.20"), totals.vat());
        assertEquals(new BigDecimal("24.20"), totals.total());
    }

    @Test
    void appliesNoDiscountWhenSubtotalExactlyAtThreshold() {
        List<LineItem> lineItems = List.of(new LineItem("Widget", 1, new BigDecimal("100.00")));

        OrderTotals totals = calculator.calculate(lineItems);

        assertEquals(new BigDecimal("0.00"), totals.discount());
        assertEquals(new BigDecimal("121.00"), totals.total());
    }

    @Test
    void appliesDiscountAboveThreshold() {
        List<LineItem> lineItems = List.of(new LineItem("Gadget", 3, new BigDecimal("50.00")));

        OrderTotals totals = calculator.calculate(lineItems);

        assertEquals(new BigDecimal("150.00"), totals.subtotal());
        assertEquals(new BigDecimal("15.00"), totals.discount());
        assertEquals(new BigDecimal("28.35"), totals.vat());
        assertEquals(new BigDecimal("163.35"), totals.total());
    }

    @Test
    void rejectsEmptyOrder() {
        assertThrows(InvalidOrderException.class, () -> calculator.calculate(List.of()));
    }
}
