// LineItemValidatorTest.java
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LineItemValidatorTest {

    private final LineItemValidator validator = new LineItemValidator();

    @Test
    void acceptsWellFormedLineItems() {
        List<RawLineItem> raw = List.of(new RawLineItem("Ticket", new BigDecimal("10.00"), 2));

        List<LineItem> result = validator.validate(raw);

        assertEquals(new BigDecimal("20.00"), result.get(0).lineTotal());
    }

    @Test
    void rejectsEmptyOrder() {
        OrderValidationException exception =
                assertThrows(OrderValidationException.class, () -> validator.validate(List.of()));

        assertTrue(exception.errors().get(0).contains("at least one line item"));
    }

    @Test
    void rejectsBlankDescription() {
        List<RawLineItem> raw = List.of(new RawLineItem(" ", new BigDecimal("10.00"), 1));

        OrderValidationException exception =
                assertThrows(OrderValidationException.class, () -> validator.validate(raw));

        assertTrue(exception.errors().get(0).contains("description"));
    }

    @Test
    void rejectsNonPositiveUnitPrice() {
        List<RawLineItem> raw = List.of(new RawLineItem("Ticket", BigDecimal.ZERO, 1));

        OrderValidationException exception =
                assertThrows(OrderValidationException.class, () -> validator.validate(raw));

        assertTrue(exception.errors().get(0).contains("unit price"));
    }

    @Test
    void rejectsNonPositiveQuantity() {
        List<RawLineItem> raw = List.of(new RawLineItem("Ticket", new BigDecimal("10.00"), 0));

        OrderValidationException exception =
                assertThrows(OrderValidationException.class, () -> validator.validate(raw));

        assertTrue(exception.errors().get(0).contains("quantity"));
    }

    @Test
    void aggregatesErrorsAcrossMultipleLineItems() {
        List<RawLineItem> raw = List.of(
                new RawLineItem("", new BigDecimal("10.00"), 1),
                new RawLineItem("Ticket", new BigDecimal("-5.00"), 1));

        OrderValidationException exception =
                assertThrows(OrderValidationException.class, () -> validator.validate(raw));

        assertEquals(2, exception.errors().size());
    }
}
