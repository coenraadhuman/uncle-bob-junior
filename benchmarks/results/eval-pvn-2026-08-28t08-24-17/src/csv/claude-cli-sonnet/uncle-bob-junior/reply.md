Assumptions: `sales.csv` has a header row, is comma-delimited with no quoted/escaped commas, the `amount` column can be anywhere in the row and is matched case-insensitively, and amount values parse as doubles.

```java
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class SalesAmountSummary {

    private static final String AMOUNT_COLUMN_NAME = "amount";
    private static final String CSV_DELIMITER = ",";

    public static void main(String[] args) throws IOException {
        Path csvPath = Path.of(args.length > 0 ? args[0] : "sales.csv");
        List<String> lines = Files.readAllLines(csvPath);
        double total = sumAmountColumn(lines);
        System.out.println(total);
    }

    static double sumAmountColumn(List<String> lines) {
        if (lines.isEmpty()) {
            return 0.0;
        }
        int amountColumnIndex = findAmountColumnIndex(lines.get(0));
        return lines.stream()
                .skip(1)
                .mapToDouble(line -> parseAmount(line, amountColumnIndex))
                .sum();
    }

    private static int findAmountColumnIndex(String headerLine) {
        String[] headers = headerLine.split(CSV_DELIMITER);
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].trim().equalsIgnoreCase(AMOUNT_COLUMN_NAME)) {
                return i;
            }
        }
        throw new IllegalArgumentException("CSV is missing an '" + AMOUNT_COLUMN_NAME + "' column");
    }

    private static double parseAmount(String line, int amountColumnIndex) {
        String[] fields = line.split(CSV_DELIMITER);
        return Double.parseDouble(fields[amountColumnIndex].trim());
    }
}
```

```java
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class SalesAmountSummaryTest {

    private static final double DELTA = 0.0001;

    @Test
    void sumsAmountsAcrossRows() {
        List<String> lines = List.of(
                "id,amount,date",
                "1,10.50,2026-01-01",
                "2,5.25,2026-01-02"
        );
        assertEquals(15.75, SalesAmountSummary.sumAmountColumn(lines), DELTA);
    }

    @Test
    void returnsZeroForEmptyFile() {
        assertEquals(0.0, SalesAmountSummary.sumAmountColumn(List.of()), DELTA);
    }

    @Test
    void findsAmountColumnRegardlessOfPosition() {
        List<String> lines = List.of(
                "date,id,amount",
                "2026-01-01,1,10.00"
        );
        assertEquals(10.00, SalesAmountSummary.sumAmountColumn(lines), DELTA);
    }

    @Test
    void throwsWhenAmountColumnMissing() {
        List<String> lines = List.of("id,date", "1,2026-01-01");
        assertThrows(IllegalArgumentException.class,
                () -> SalesAmountSummary.sumAmountColumn(lines));
    }
}
```

`sumAmountColumn()` → cleaned: pure function separated from file I/O, header lookup and row parsing each do one job, guard clause on empty input, no magic column index. Safe to change because tests cover happy path, empty file, reordered columns, and the missing-column error.