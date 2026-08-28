import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SalesAmountSummerTest {

    @Test
    void sumsAmountsAcrossRows() {
        List<String> lines = List.of("id,amount", "1,10.50", "2,5.25");
        assertEquals(15.75, SalesAmountSummer.sumAmountColumn(lines), 0.001);
    }

    @Test
    void handlesColumnOrderAndCaseInsensitiveHeader() {
        List<String> lines = List.of("Amount,id", "10,1", "20,2");
        assertEquals(30.0, SalesAmountSummer.sumAmountColumn(lines), 0.001);
    }

    @Test
    void returnsZeroForHeaderOnlyFile() {
        List<String> lines = List.of("id,amount");
        assertEquals(0.0, SalesAmountSummer.sumAmountColumn(lines), 0.001);
    }

    @Test
    void throwsWhenAmountColumnMissing() {
        List<String> lines = List.of("id,price", "1,10");
        assertThrows(IllegalArgumentException.class, () -> SalesAmountSummer.sumAmountColumn(lines));
    }

    @Test
    void throwsWhenCsvIsEmpty() {
        assertThrows(IllegalArgumentException.class, () -> SalesAmountSummer.sumAmountColumn(List.of()));
    }
}
