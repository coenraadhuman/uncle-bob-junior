import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SalesTotalTest {

    @Test
    void sumsAmountsAcrossRows() {
        List<String> lines = List.of(
                "id,amount,note",
                "1,10.50,a",
                "2,5.25,b"
        );

        assertEquals(15.75, SalesTotal.sumAmountColumn(lines), 0.0001);
    }

    @Test
    void findsAmountColumnRegardlessOfPosition() {
        List<String> lines = List.of(
                "amount,id",
                "3.00,1"
        );

        assertEquals(3.00, SalesTotal.sumAmountColumn(lines), 0.0001);
    }

    @Test
    void returnsZeroForHeaderOnlyFile() {
        List<String> lines = List.of("id,amount");

        assertEquals(0.0, SalesTotal.sumAmountColumn(lines), 0.0001);
    }

    @Test
    void returnsZeroForEmptyFile() {
        assertEquals(0.0, SalesTotal.sumAmountColumn(List.of()), 0.0001);
    }

    @Test
    void skipsBlankLines() {
        List<String> lines = List.of(
                "id,amount",
                "1,2.00",
                "",
                "2,3.00"
        );

        assertEquals(5.00, SalesTotal.sumAmountColumn(lines), 0.0001);
    }

    @Test
    void throwsWhenAmountColumnMissing() {
        List<String> lines = List.of("id,note", "1,a");

        assertThrows(IllegalArgumentException.class, () -> SalesTotal.sumAmountColumn(lines));
    }
}
