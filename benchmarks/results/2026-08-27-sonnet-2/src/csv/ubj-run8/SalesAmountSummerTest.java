import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SalesAmountSummerTest {

    @TempDir
    Path tempDir;

    @Test
    void sumsAmountsAcrossMultipleRows() throws IOException {
        Path csv = writeCsv("id,amount\n1,10.50\n2,20.25\n3,5.00\n");

        double result = SalesAmountSummer.sumAmountColumn(csv);

        assertEquals(35.75, result, 0.0001);
    }

    @Test
    void returnsZeroWhenOnlyHeaderPresent() throws IOException {
        Path csv = writeCsv("id,amount\n");

        double result = SalesAmountSummer.sumAmountColumn(csv);

        assertEquals(0.0, result, 0.0001);
    }

    @Test
    void findsAmountColumnRegardlessOfPosition() throws IOException {
        Path csv = writeCsv("amount,id\n7.00,1\n3.00,2\n");

        double result = SalesAmountSummer.sumAmountColumn(csv);

        assertEquals(10.0, result, 0.0001);
    }

    @Test
    void throwsWhenAmountColumnMissing() throws IOException {
        Path csv = writeCsv("id,total\n1,10\n");

        assertThrows(IllegalArgumentException.class, () -> SalesAmountSummer.sumAmountColumn(csv));
    }

    @Test
    void skipsBlankLines() throws IOException {
        Path csv = writeCsv("id,amount\n1,10\n\n2,15\n");

        double result = SalesAmountSummer.sumAmountColumn(csv);

        assertEquals(25.0, result, 0.0001);
    }

    private Path writeCsv(String content) throws IOException {
        Path file = tempDir.resolve("sales.csv");
        Files.writeString(file, content);
        return file;
    }
}
