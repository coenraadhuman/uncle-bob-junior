import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SalesAmountSummerTest {

    @TempDir
    Path tempDir;

    @Test
    void sumsAmountColumnAcrossRows() throws IOException {
        Path csv = writeCsv("id,amount,date",
                "1,10.50,2026-01-01",
                "2,20.25,2026-01-02",
                "3,5.00,2026-01-03");

        BigDecimal total = SalesAmountSummer.sumAmountColumn(csv);

        assertEquals(new BigDecimal("35.75"), total);
    }

    @Test
    void ignoresBlankLines() throws IOException {
        Path csv = writeCsv("id,amount",
                "1,10.00",
                "",
                "2,5.00");

        BigDecimal total = SalesAmountSummer.sumAmountColumn(csv);

        assertEquals(new BigDecimal("15.00"), total);
    }

    @Test
    void findsAmountColumnRegardlessOfPosition() throws IOException {
        Path csv = writeCsv("date,id,amount",
                "2026-01-01,1,7.50",
                "2026-01-02,2,2.50");

        BigDecimal total = SalesAmountSummer.sumAmountColumn(csv);

        assertEquals(new BigDecimal("10.00"), total);
    }

    @Test
    void returnsZeroWhenOnlyHeaderPresent() throws IOException {
        Path csv = writeCsv("id,amount");

        BigDecimal total = SalesAmountSummer.sumAmountColumn(csv);

        assertEquals(BigDecimal.ZERO, total);
    }

    @Test
    void throwsWhenAmountColumnMissing() throws IOException {
        Path csv = writeCsv("id,date",
                "1,2026-01-01");

        assertThrows(IllegalArgumentException.class, () -> SalesAmountSummer.sumAmountColumn(csv));
    }

    @Test
    void throwsWhenFileIsEmpty() throws IOException {
        Path csv = writeCsv();

        assertThrows(IllegalArgumentException.class, () -> SalesAmountSummer.sumAmountColumn(csv));
    }

    private Path writeCsv(String... lines) throws IOException {
        Path file = tempDir.resolve("sales.csv");
        Files.write(file, java.util.List.of(lines));
        return file;
    }
}
