// SalesAmountSummerTest.java
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SalesAmountSummerTest {

    @Test
    void sumsAmountsAcrossRows() {
        List<String> lines = List.of(
                "id,amount,region",
                "1,10.50,NL",
                "2,5.25,UK");

        assertEquals(15.75, SalesAmountSummer.sumAmountColumn(lines), 0.0001);
    }

    @Test
    void findsAmountColumnRegardlessOfPosition() {
        List<String> lines = List.of(
                "region,id,amount",
                "NL,1,10.00");

        assertEquals(10.00, SalesAmountSummer.sumAmountColumn(lines), 0.0001);
    }

    @Test
    void returnsZeroWhenFileHasOnlyHeader() {
        List<String> lines = List.of("id,amount,region");

        assertEquals(0.0, SalesAmountSummer.sumAmountColumn(lines), 0.0001);
    }

    @Test
    void returnsZeroWhenFileIsEmpty() {
        assertEquals(0.0, SalesAmountSummer.sumAmountColumn(List.of()), 0.0001);
    }

    @Test
    void throwsWhenAmountColumnIsMissing() {
        List<String> lines = List.of("id,region", "1,NL");

        assertThrows(IllegalArgumentException.class,
                () -> SalesAmountSummer.sumAmountColumn(lines));
    }
}
