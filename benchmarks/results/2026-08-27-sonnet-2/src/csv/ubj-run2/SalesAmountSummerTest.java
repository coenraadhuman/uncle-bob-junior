// SalesAmountSummerTest.java (JUnit 5)
import org.junit.jupiter.api.Test;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SalesAmountSummerTest {

    @Test
    void sumsAmountsAcrossRows() throws IOException {
        String csv = "id,amount\n1,10.50\n2,5.25\n";
        assertEquals(15.75, sum(csv), 0.0001);
    }

    @Test
    void findsAmountColumnRegardlessOfPosition() throws IOException {
        String csv = "amount,id\n10.00,1\n20.00,2\n";
        assertEquals(30.00, sum(csv), 0.0001);
    }

    @Test
    void returnsZeroForHeaderOnlyFile() throws IOException {
        String csv = "id,amount\n";
        assertEquals(0.0, sum(csv), 0.0001);
    }

    @Test
    void returnsZeroForEmptyFile() throws IOException {
        assertEquals(0.0, sum(""), 0.0001);
    }

    @Test
    void throwsWhenAmountColumnMissing() {
        String csv = "id,description\n1,widget\n";
        assertThrows(IllegalArgumentException.class, () -> sum(csv));
    }

    private double sum(String csv) throws IOException {
        try (BufferedReader reader = new BufferedReader(new StringReader(csv))) {
            return SalesAmountSummer.sumAmountColumn(reader);
        }
    }
}
