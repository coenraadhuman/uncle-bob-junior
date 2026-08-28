// LineItemTest.java
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LineItemTest {

    @Test
    void rejectsBlankDescription() {
        assertThrows(IllegalArgumentException.class,
            () -> new LineItem("  ", new BigDecimal("10.00"), 1));
    }

    @Test
    void rejectsNegativePrice() {
        assertThrows(IllegalArgumentException.class,
            () -> new LineItem("participant_001", new BigDecimal("-1.00"), 1));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void rejectsNonPositiveQuantity(int quantity) {
        assertThrows(IllegalArgumentException.class,
            () -> new LineItem("participant_001", new BigDecimal("10.00"), quantity));
    }

    @Test
    void lineTotalMultipliesPriceByQuantity() {
        LineItem item = new LineItem("participant_001", new BigDecimal("10.00"), 3);

        assertEquals(new BigDecimal("30.00"), item.lineTotal());
    }
}
