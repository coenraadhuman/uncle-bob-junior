import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SalesTotalTest {

    @Test
    void sumsAmountColumn() {
        List<String> csv = List.of(
                "id,amount,region",
                "participant_001,10.50,NL",
                "participant_002,4.25,NL");
        assertEquals(new BigDecimal("14.75"), SalesTotal.sumAmountColumn(csv));
    }

    @Test
    void findsAmountColumnRegardlessOfPositionAndCase() {
        List<String> csv = List.of(
                "Amount,id",
                "3.00,participant_001");
        assertEquals(new BigDecimal("3.00"), SalesTotal.sumAmountColumn(csv));
    }

    @Test
    void headerOnlyFileSumsToZero() {
        assertEquals(BigDecimal.ZERO, SalesTotal.sumAmountColumn(List.of("id,amount")));
    }

    @Test
    void skipsBlankLines() {
        List<String> csv = List.of("id,amount", "participant_001,2.00", "", "  ");
        assertEquals(new BigDecimal("2.00"), SalesTotal.sumAmountColumn(csv));
    }

    @Test
    void sumsNegativeAmounts() {
        List<String> csv = List.of("id,amount", "participant_001,5.00", "participant_002,-2.50");
        assertEquals(new BigDecimal("2.50"), SalesTotal.sumAmountColumn(csv));
    }

    @Test
    void rejectsEmptyFile() {
        assertThrows(IllegalArgumentException.class,
                () -> SalesTotal.sumAmountColumn(List.of()));
    }

    @Test
    void rejectsMissingAmountColumn() {
        assertThrows(IllegalArgumentException.class,
                () -> SalesTotal.sumAmountColumn(List.of("id,total", "participant_001,1.00")));
    }

    @Test
    void rejectsRowShorterThanAmountIndex() {
        assertThrows(IllegalArgumentException.class,
                () -> SalesTotal.sumAmountColumn(List.of("id,amount", "participant_001")));
    }

    @Test
    void rejectsNonNumericAmount() {
        assertThrows(NumberFormatException.class,
                () -> SalesTotal.sumAmountColumn(List.of("id,amount", "participant_001,abc")));
    }
}
