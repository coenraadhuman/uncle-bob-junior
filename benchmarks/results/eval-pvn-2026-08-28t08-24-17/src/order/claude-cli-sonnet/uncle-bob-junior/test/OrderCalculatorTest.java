// OrderCalculatorTest.java
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderCalculatorTest {

    private final OrderCalculator calculator = new OrderCalculator();

    @Test
    void throwsOnEmptyOrder() {
        assertThrows(IllegalArgumentException.class, () -> calculator.summarize(List.of()));
    }

    @Test
    void throwsOnNullOrder() {
        assertThrows(NullPointerException.class, () -> calculator.summarize(null));
    }

    @Test
    void noDiscountWhenSubtotalDoesNotExceedThreshold() {
        LineItem item = new LineItem("participant_001", new BigDecimal("50.00"), 2);

        OrderSummary summary = calculator.summarize(List.of(item));

        assertEquals(new BigDecimal("100.00"), summary.subtotal());
        assertEquals(new BigDecimal("0.00"), summary.discount());
        assertEquals(new BigDecimal("21.00"), summary.vatAmount());
        assertEquals(new BigDecimal("121.00"), summary.total());
    }

    @Test
    void appliesDiscountWhenSubtotalExceedsThreshold() {
        LineItem item = new LineItem("participant_001", new BigDecimal("60.00"), 2);

        OrderSummary summary = calculator.summarize(List.of(item));

        assertEquals(new BigDecimal("120.00"), summary.subtotal());
        assertEquals(new BigDecimal("12.00"), summary.discount());
        assertEquals(new BigDecimal("22.68"), summary.vatAmount());
        assertEquals(new BigDecimal("130.68"), summary.total());
    }
}
