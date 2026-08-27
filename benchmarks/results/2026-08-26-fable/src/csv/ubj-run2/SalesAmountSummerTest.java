import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SalesAmountSummerTest {

    @Test
    void sumsAmountColumnAcrossRows() {
        List<String> lines = List.of(
                "id,amount,region",
                "1,10.50,NL",
                "2,4.25,UK");
        assertEquals(new BigDecimal("14.75"), SalesAmountSummer.sumAmountColumn(lines));
    }

    @Test
    void headerOnlyFileSumsToZero() {
        assertEquals(BigDecimal.ZERO, SalesAmountSummer.sumAmountColumn(List.of("id,amount")));
    }

    @Test
    void skipsBlankLines() {
        List<String> lines = List.of("amount", "3", "", "4");
        assertEquals(new BigDecimal("7"), SalesAmountSummer.sumAmountColumn(lines));
    }

    @Test
    void rejectsEmptyFile() {
        assertThrows(IllegalArgumentException.class,
                () -> SalesAmountSummer.sumAmountColumn(List.of()));
    }

    @Test
    void rejectsMissingAmountColumn() {
        assertThrows(IllegalArgumentException.class,
                () -> SalesAmountSummer.sumAmountColumn(List.of("id,total", "1,5")));
    }

    @Test
    void rejectsNonNumericAmountWithLineNumber() {
        List<String> lines = List.of("amount", "abc");
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> SalesAmountSummer.sumAmountColumn(lines));
        assertEquals("Line 2 has a non-numeric amount: abc", e.getMessage());
    }
}
