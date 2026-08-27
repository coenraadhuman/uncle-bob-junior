import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SalesTotalTest {

    @Test
    void sumsAmountColumnWherever_itAppears() {
        List<String> lines = List.of(
                "id,amount,region",
                "participant_001,10.50,NL",
                "participant_002,4.25,NL");
        assertEquals(new BigDecimal("14.75"), SalesTotal.sumAmountColumn(lines));
    }

    @Test
    void headerMatchIsCaseInsensitive() {
        List<String> lines = List.of("Amount", "1.00", "2.00");
        assertEquals(new BigDecimal("3.00"), SalesTotal.sumAmountColumn(lines));
    }

    @Test
    void headerOnlyFileSumsToZero() {
        assertEquals(BigDecimal.ZERO, SalesTotal.sumAmountColumn(List.of("amount")));
    }

    @Test
    void blankLinesAreSkipped() {
        List<String> lines = List.of("amount", "5.00", "", "7.00");
        assertEquals(new BigDecimal("12.00"), SalesTotal.sumAmountColumn(lines));
    }

    @Test
    void emptyFileIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> SalesTotal.sumAmountColumn(List.of()));
    }

    @Test
    void missingAmountHeaderIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> SalesTotal.sumAmountColumn(List.of("id,price", "1,2.00")));
    }

    @Test
    void rowMissingTheAmountCellIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> SalesTotal.sumAmountColumn(List.of("id,amount", "participant_001")));
    }

    @Test
    void nonNumericAmountIsRejected() {
        assertThrows(NumberFormatException.class,
                () -> SalesTotal.sumAmountColumn(List.of("amount", "not-a-number")));
    }
}
