import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SalesTotalTest {

    @Test
    void sumsAmountsAcrossRows() throws IOException {
        String csv = "id,amount\n1,10.5\n2,4.5\n";
        assertEquals(15.0, sum(csv), 0.0001);
    }

    @Test
    void handlesAmountColumnNotFirst() throws IOException {
        String csv = "name,amount,region\nAlice,20,NL\nBob,5,UK\n";
        assertEquals(25.0, sum(csv), 0.0001);
    }

    @Test
    void headerOnlyReturnsZero() throws IOException {
        String csv = "id,amount\n";
        assertEquals(0.0, sum(csv), 0.0001);
    }

    @Test
    void emptyFileReturnsZero() throws IOException {
        assertEquals(0.0, sum(""), 0.0001);
    }

    @Test
    void missingAmountColumnThrows() {
        String csv = "id,total\n1,10\n";
        assertThrows(IllegalArgumentException.class, () -> sum(csv));
    }

    private double sum(String csv) throws IOException {
        try (BufferedReader reader = new BufferedReader(new StringReader(csv))) {
            return SalesTotal.sumAmountColumn(reader);
        }
    }
}
