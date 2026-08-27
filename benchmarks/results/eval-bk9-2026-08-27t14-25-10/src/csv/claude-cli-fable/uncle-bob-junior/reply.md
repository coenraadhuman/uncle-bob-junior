Assumptions: `sales.csv` is in the working directory, has a header row naming an `amount` column (any position, case-insensitive), fields are comma-separated without quoted commas, and amounts are plain decimals. `BigDecimal` is used since amounts look like money.

```java
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class SalesTotal {

    private static final Path SALES_FILE = Path.of("sales.csv");
    private static final String COLUMN_SEPARATOR = ",";
    private static final String AMOUNT_COLUMN = "amount";

    public static void main(String[] args) throws IOException {
        List<String> lines = Files.readAllLines(SALES_FILE);
        System.out.println(sumAmountColumn(lines));
    }

    static BigDecimal sumAmountColumn(List<String> lines) {
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("CSV is empty: a header row is required");
        }
        int amountIndex = amountColumnIndex(lines.get(0));
        BigDecimal total = BigDecimal.ZERO;
        for (String row : lines.subList(1, lines.size())) {
            if (row.isBlank()) {
                continue;
            }
            total = total.add(amountIn(row, amountIndex));
        }
        return total;
    }

    private static int amountColumnIndex(String headerRow) {
        String[] headers = headerRow.split(COLUMN_SEPARATOR, -1);
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].trim().equalsIgnoreCase(AMOUNT_COLUMN)) {
                return i;
            }
        }
        throw new IllegalArgumentException(
                "No '" + AMOUNT_COLUMN + "' column in header: " + headerRow);
    }

    private static BigDecimal amountIn(String row, int amountIndex) {
        String[] cells = row.split(COLUMN_SEPARATOR, -1);
        if (amountIndex >= cells.length) {
            throw new IllegalArgumentException("Row is missing the amount cell: " + row);
        }
        return new BigDecimal(cells[amountIndex].trim());
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
    void sumsAmountColumnWherever_itAppears() {
        List<String> lines = List.of(
                "id,amount,region",
                "participant_001,10.50,NL",
                "participant_002,4.25,NL");
        assertEquals(new BigDecimal("14.75"), SalesTotal.sumAmountColumn(lines));
    }

    @Test
    void headerMatchIsCaseInsensitive() {
        List<String> lines = List.of("Amount", "1.00", "2.00");
        assertEquals(new BigDecimal("3.00"), SalesTotal.sumAmountColumn(lines));
    }

    @Test
    void headerOnlyFileSumsToZero() {
        assertEquals(BigDecimal.ZERO, SalesTotal.sumAmountColumn(List.of("amount")));
    }

    @Test
    void blankLinesAreSkipped() {
        List<String> lines = List.of("amount", "5.00", "", "7.00");
        assertEquals(new BigDecimal("12.00"), SalesTotal.sumAmountColumn(lines));
    }

    @Test
    void emptyFileIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> SalesTotal.sumAmountColumn(List.of()));
    }

    @Test
    void missingAmountHeaderIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> SalesTotal.sumAmountColumn(List.of("id,price", "1,2.00")));
    }

    @Test
    void rowMissingTheAmountCellIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> SalesTotal.sumAmountColumn(List.of("id,amount", "participant_001")));
    }

    @Test
    void nonNumericAmountIsRejected() {
        assertThrows(NumberFormatException.class,
                () -> SalesTotal.sumAmountColumn(List.of("amount", "not-a-number")));
    }
}
```

Compile with `javac SalesTotal.java` and run with `java SalesTotal` from the directory containing `sales.csv`.

Cleaned: pure `sumAmountColumn` core testable without touching the filesystem, header lookup and cell parsing extracted with guard clauses, and `BigDecimal` avoids floating-point drift on money. Safe to change because every branch (happy path, blank lines, missing header, missing cell, bad number, empty file) is pinned by a test.