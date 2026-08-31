```java
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class SalesAmountSummary {

    private static final String DEFAULT_CSV_FILE_NAME = "sales.csv";
    private static final String COLUMN_DELIMITER = ",";
    private static final String AMOUNT_COLUMN_NAME = "amount";

    private SalesAmountSummary() {
    }

    public static void main(String[] args) throws IOException {
        Path csvPath = Path.of(args.length > 0 ? args[0] : DEFAULT_CSV_FILE_NAME);
        BigDecimal total = sumAmountColumn(csvPath);
        System.out.println(total);
    }

    static BigDecimal sumAmountColumn(Path csvPath) throws IOException {
        List<String> lines = Files.readAllLines(csvPath);
        if (lines.isEmpty()) {
            return BigDecimal.ZERO;
        }

        int amountColumnIndex = findAmountColumnIndex(lines.get(0));
        BigDecimal total = BigDecimal.ZERO;
        for (String line : lines.subList(1, lines.size())) {
            total = total.add(parseAmount(line, amountColumnIndex));
        }
        return total;
    }

    private static int findAmountColumnIndex(String headerLine) {
        String[] headers = headerLine.split(COLUMN_DELIMITER);
        for (int index = 0; index < headers.length; index++) {
            if (headers[index].trim().equalsIgnoreCase(AMOUNT_COLUMN_NAME)) {
                return index;
            }
        }
        throw new IllegalArgumentException("CSV header is missing an '" + AMOUNT_COLUMN_NAME + "' column");
    }

    private static BigDecimal parseAmount(String line, int amountColumnIndex) {
        if (line.isBlank()) {
            return BigDecimal.ZERO;
        }
        String[] fields = line.split(COLUMN_DELIMITER);
        return new BigDecimal(fields[amountColumnIndex].trim());
    }
}
```

```java
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
```

Assumptions: the CSV has a header row, is comma-delimited with no quoted or embedded-comma fields, and the `amount` column holds plain decimal numbers (no currency symbols or thousands separators) parseable by `BigDecimal`. `sumAmountColumn()` looks up the column by header name rather than a fixed index, uses `BigDecimal` for exact money arithmetic, and skips blank lines.

`SalesAmountSummary.sumAmountColumn()` isolates header lookup and row parsing into two small functions → cleaned: intent-revealing names, guard clauses for empty file/blank line, no magic literals, safe to change because header order and blank lines no longer break the sum.