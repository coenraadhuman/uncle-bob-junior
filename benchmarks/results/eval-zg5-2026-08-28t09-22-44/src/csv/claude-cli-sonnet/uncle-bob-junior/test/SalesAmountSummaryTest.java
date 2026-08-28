import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SalesAmountSummaryTest {

    @TempDir
    Path tempDir;

    @Test
    void sumsAmountColumnAcrossMultipleRows() throws IOException {
        Path csvPath = writeCsv("date,amount,region\n2026-01-01,10.50,NL\n2026-01-02,5.25,UK\n");

        BigDecimal total = SalesAmountSummary.sumAmountColumn(csvPath);

        assertEquals(new BigDecimal("15.75"), total);
    }

    @Test
    void findsAmountColumnRegardlessOfPosition() throws IOException {
        Path csvPath = writeCsv("region,amount\nNL,10\nUK,20\n");

        BigDecimal total = SalesAmountSummary.sumAmountColumn(csvPath);

        assertEquals(new BigDecimal("30"), total);
    }

    @Test
    void skipsBlankLines() throws IOException {
        Path csvPath = writeCsv("amount\n10\n\n20\n");

        BigDecimal total = SalesAmountSummary.sumAmountColumn(csvPath);

        assertEquals(new BigDecimal("30"), total);
    }

    @Test
    void returnsZeroForHeaderOnlyFile() throws IOException {
        Path csvPath = writeCsv("date,amount\n");

        BigDecimal total = SalesAmountSummary.sumAmountColumn(csvPath);

        assertEquals(BigDecimal.ZERO, total);
    }

    @Test
    void returnsZeroForEmptyFile() throws IOException {
        Path csvPath = writeCsv("");

        BigDecimal total = SalesAmountSummary.sumAmountColumn(csvPath);

        assertEquals(BigDecimal.ZERO, total);
    }

    @Test
    void throwsWhenAmountColumnIsMissing() throws IOException {
        Path csvPath = writeCsv("date,region\n2026-01-01,NL\n");

        assertThrows(IllegalArgumentException.class, () -> SalesAmountSummary.sumAmountColumn(csvPath));
    }

    private Path writeCsv(String content) throws IOException {
        Path csvPath = tempDir.resolve("sales.csv");
        Files.writeString(csvPath, content);
        return csvPath;
    }
}
