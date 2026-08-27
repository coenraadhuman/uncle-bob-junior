import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import org.junit.jupiter.api.Test;

class SalesAmountSummerTest {

    @Test
    void sumsAmountsAcrossRows() throws IOException {
        String csv = "id,amount,date\n1,10.50,2026-01-01\n2,20.25,2026-01-02\n";
        double total = SalesAmountSummer.sumAmountColumn(new BufferedReader(new StringReader(csv)));
        assertEquals(30.75, total, 0.0001);
    }

    @Test
    void ignoresColumnOrderAndUsesHeaderName() throws IOException {
        String csv = "date,amount,id\n2026-01-01,5.00,1\n";
        double total = SalesAmountSummer.sumAmountColumn(new BufferedReader(new StringReader(csv)));
        assertEquals(5.00, total, 0.0001);
    }

    @Test
    void returnsZeroForEmptyFile() throws IOException {
        double total = SalesAmountSummer.sumAmountColumn(new BufferedReader(new StringReader("")));
        assertEquals(0.0, total, 0.0001);
    }

    @Test
    void skipsBlankLines() throws IOException {
        String csv = "id,amount\n1,10\n\n2,20\n";
        double total = SalesAmountSummer.sumAmountColumn(new BufferedReader(new StringReader(csv)));
        assertEquals(30.0, total, 0.0001);
    }

    @Test
    void throwsWhenAmountColumnMissing() {
        String csv = "id,value\n1,10\n";
        assertThrows(IllegalArgumentException.class,
            () -> SalesAmountSummer.sumAmountColumn(new BufferedReader(new StringReader(csv))));
    }
}
