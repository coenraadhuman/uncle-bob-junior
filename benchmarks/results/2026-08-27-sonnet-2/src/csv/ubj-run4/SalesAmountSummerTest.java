// SalesAmountSummerTest.java
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SalesAmountSummerTest {

    private final SalesAmountSummer summer = new SalesAmountSummer();

    @Test
    void sumsAmountColumnAcrossRows() {
        List<String> lines = List.of(
                "id,amount,customer",
                "1,10.50,alice",
                "2,5.25,bob"
        );

        assertEquals(new BigDecimal("15.75"), summer.sum(lines));
    }

    @Test
    void findsAmountColumnRegardlessOfPosition() {
        List<String> lines = List.of(
                "customer,amount",
                "alice,10.00"
        );

        assertEquals(new BigDecimal("10.00"), summer.sum(lines));
    }

    @Test
    void skipsBlankLines() {
        List<String> lines = List.of(
                "id,amount",
                "1,10.00",
                "",
                "2,5.00"
        );

        assertEquals(new BigDecimal("15.00"), summer.sum(lines));
    }

    @Test
    void returnsZeroWhenOnlyHeaderPresent() {
        List<String> lines = List.of("id,amount");

        assertEquals(BigDecimal.ZERO, summer.sum(lines));
    }

    @Test
    void returnsZeroForEmptyFile() {
        assertEquals(BigDecimal.ZERO, summer.sum(List.of()));
    }

    @Test
    void throwsWhenAmountColumnIsMissing() {
        List<String> lines = List.of("id,customer", "1,alice");

        assertThrows(IllegalArgumentException.class, () -> summer.sum(lines));
    }
}
