import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class SalesAmountSummerTest {
    private static final double DELTA = 0.0001;
    private final SalesAmountSummer summer = new SalesAmountSummer();

    @Test
    void sumsAmountsAcrossRows() {
        List<String> lines = List.of("id,amount", "1,10.50", "2,20.25");
        assertEquals(30.75, summer.sumAmountColumn(lines), DELTA);
    }

    @Test
    void returnsZeroForHeaderOnlyCsv() {
        List<String> lines = List.of("id,amount");
        assertEquals(0.0, summer.sumAmountColumn(lines), DELTA);
    }

    @Test
    void returnsZeroForEmptyCsv() {
        assertEquals(0.0, summer.sumAmountColumn(List.of()), DELTA);
    }

    @Test
    void findsAmountColumnRegardlessOfPosition() {
        List<String> lines = List.of("amount,id", "5.00,1");
        assertEquals(5.00, summer.sumAmountColumn(lines), DELTA);
    }

    @Test
    void throwsWhenAmountColumnMissing() {
        List<String> lines = List.of("id,price", "1,10");
        assertThrows(IllegalArgumentException.class, () -> summer.sumAmountColumn(lines));
    }
}
