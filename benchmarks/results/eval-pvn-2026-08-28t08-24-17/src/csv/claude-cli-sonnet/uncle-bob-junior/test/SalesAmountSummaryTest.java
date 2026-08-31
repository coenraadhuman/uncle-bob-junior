import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class SalesAmountSummaryTest {

    private static final double DELTA = 0.0001;

    @Test
    void sumsAmountsAcrossRows() {
        List<String> lines = List.of(
                "id,amount,date",
                "1,10.50,2026-01-01",
                "2,5.25,2026-01-02"
        );
        assertEquals(15.75, SalesAmountSummary.sumAmountColumn(lines), DELTA);
    }

    @Test
    void returnsZeroForEmptyFile() {
        assertEquals(0.0, SalesAmountSummary.sumAmountColumn(List.of()), DELTA);
    }

    @Test
    void findsAmountColumnRegardlessOfPosition() {
        List<String> lines = List.of(
                "date,id,amount",
                "2026-01-01,1,10.00"
        );
        assertEquals(10.00, SalesAmountSummary.sumAmountColumn(lines), DELTA);
    }

    @Test
    void throwsWhenAmountColumnMissing() {
        List<String> lines = List.of("id,date", "1,2026-01-01");
        assertThrows(IllegalArgumentException.class,
                () -> SalesAmountSummary.sumAmountColumn(lines));
    }
}
