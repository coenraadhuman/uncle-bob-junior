Assumptions: `sales.csv` is in the working directory with a header row, plain comma-separated fields (no quoted commas), and decimal amounts. `BigDecimal` is used because this is a money path. Standard library only, since a snippet can't take a CSV dependency; if the project has Maven/Gradle, swapping the parsing for commons-csv is the better long-term move.

```java
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Prints the sum of the 'amount' column of sales.csv.
 * Expects a header row; blank lines are skipped.
 * ubj: naive split-based CSV, replace with commons-csv if fields ever contain quoted commas.
 */
public final class SalesTotal {

    private static final Path SALES_FILE = Path.of("sales.csv");
    private static final String AMOUNT_COLUMN = "amount";
    private static final String DELIMITER = ",";
    private static final int HEADER_ROW_COUNT = 1;

    public static void main(String[] args) throws IOException {
        List<String> lines = Files.readAllLines(SALES_FILE);
        System.out.println(sumAmountColumn(lines));
    }

    static BigDecimal sumAmountColumn(List<String> csvLines) {
        if (csvLines.isEmpty()) {
            throw new IllegalArgumentException("CSV is empty: a header row is required");
        }
        int amountIndex = columnIndex(csvLines.get(0), AMOUNT_COLUMN);
        return csvLines.stream()
                .skip(HEADER_ROW_COUNT)
                .filter(line -> !line.isBlank())
                .map(line -> amountIn(line, amountIndex))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static int columnIndex(String headerLine, String columnName) {
        String[] headers = headerLine.split(DELIMITER, -1);
        for (int index = 0; index < headers.length; index++) {
            if (headers[index].trim().equalsIgnoreCase(columnName)) {
                return index;
            }
        }
        throw new IllegalArgumentException(
                "Missing '" + columnName + "' column in header: " + headerLine);
    }

    private static BigDecimal amountIn(String csvLine, int amountIndex) {
        String[] fields = csvLine.split(DELIMITER, -1);
        if (amountIndex >= fields.length) {
            throw new IllegalArgumentException("Row has no amount field: " + csvLine);
        }
        return new BigDecimal(fields[amountIndex].trim());
    }

    private SalesTotal() {
    }
}
```

Tests (JUnit 5):

```java
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SalesTotalTest {

    @Test
    void sumsAmountColumn() {
        List<String> csv = List.of(
                "id,amount,region",
                "participant_001,10.50,NL",
                "participant_002,4.25,NL");
        assertEquals(new BigDecimal("14.75"), SalesTotal.sumAmountColumn(csv));
    }

    @Test
    void findsAmountColumnRegardlessOfPositionAndCase() {
        List<String> csv = List.of(
                "Amount,id",
                "3.00,participant_001");
        assertEquals(new BigDecimal("3.00"), SalesTotal.sumAmountColumn(csv));
    }

    @Test
    void headerOnlyFileSumsToZero() {
        assertEquals(BigDecimal.ZERO, SalesTotal.sumAmountColumn(List.of("id,amount")));
    }

    @Test
    void skipsBlankLines() {
        List<String> csv = List.of("id,amount", "participant_001,2.00", "", "  ");
        assertEquals(new BigDecimal("2.00"), SalesTotal.sumAmountColumn(csv));
    }

    @Test
    void sumsNegativeAmounts() {
        List<String> csv = List.of("id,amount", "participant_001,5.00", "participant_002,-2.50");
        assertEquals(new BigDecimal("2.50"), SalesTotal.sumAmountColumn(csv));
    }

    @Test
    void rejectsEmptyFile() {
        assertThrows(IllegalArgumentException.class,
                () -> SalesTotal.sumAmountColumn(List.of()));
    }

    @Test
    void rejectsMissingAmountColumn() {
        assertThrows(IllegalArgumentException.class,
                () -> SalesTotal.sumAmountColumn(List.of("id,total", "participant_001,1.00")));
    }

    @Test
    void rejectsRowShorterThanAmountIndex() {
        assertThrows(IllegalArgumentException.class,
                () -> SalesTotal.sumAmountColumn(List.of("id,amount", "participant_001")));
    }

    @Test
    void rejectsNonNumericAmount() {
        assertThrows(NumberFormatException.class,
                () -> SalesTotal.sumAmountColumn(List.of("id,amount", "participant_001,abc")));
    }
}
```

Cleaned: file I/O isolated in `main` so the summing core is pure and fully testable; header lookup, row parsing, and summing each do one job. Safe to change because the amount column is found by name, not position, and every edge (empty file, missing column, short row, bad number) fails loudly with a test proving it.