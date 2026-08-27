Assumptions: `sales.csv` is comma-delimited with a header row, sits in the working directory, has a column named `amount` (case-insensitive, any position), and amount values parse as `double`. Blank lines are skipped.

```java
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class SalesTotal {

    private static final String SALES_CSV_PATH = "sales.csv";
    private static final String COLUMN_DELIMITER = ",";
    private static final String AMOUNT_COLUMN_NAME = "amount";

    public static void main(String[] args) throws IOException {
        List<String> lines = Files.readAllLines(Path.of(SALES_CSV_PATH));
        System.out.println(sumAmountColumn(lines));
    }

    static double sumAmountColumn(List<String> lines) {
        if (lines.isEmpty()) {
            return 0.0;
        }
        int amountColumnIndex = findAmountColumnIndex(lines.get(0));
        double total = 0.0;
        for (String line : lines.subList(1, lines.size())) {
            if (line.isBlank()) {
                continue;
            }
            total += parseAmount(line, amountColumnIndex);
        }
        return total;
    }

    private static int findAmountColumnIndex(String headerLine) {
        String[] headers = headerLine.split(COLUMN_DELIMITER);
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].trim().equalsIgnoreCase(AMOUNT_COLUMN_NAME)) {
                return i;
            }
        }
        throw new IllegalArgumentException("CSV is missing an '" + AMOUNT_COLUMN_NAME + "' column");
    }

    private static double parseAmount(String line, int amountColumnIndex) {
        String[] fields = line.split(COLUMN_DELIMITER);
        return Double.parseDouble(fields[amountColumnIndex].trim());
    }
}
```

```java
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SalesTotalTest {

    @Test
    void sumsAmountsAcrossRows() {
        List<String> lines = List.of(
                "id,amount,note",
                "1,10.50,a",
                "2,5.25,b"
        );

        assertEquals(15.75, SalesTotal.sumAmountColumn(lines), 0.0001);
    }

    @Test
    void findsAmountColumnRegardlessOfPosition() {
        List<String> lines = List.of(
                "amount,id",
                "3.00,1"
        );

        assertEquals(3.00, SalesTotal.sumAmountColumn(lines), 0.0001);
    }

    @Test
    void returnsZeroForHeaderOnlyFile() {
        List<String> lines = List.of("id,amount");

        assertEquals(0.0, SalesTotal.sumAmountColumn(lines), 0.0001);
    }

    @Test
    void returnsZeroForEmptyFile() {
        assertEquals(0.0, SalesTotal.sumAmountColumn(List.of()), 0.0001);
    }

    @Test
    void skipsBlankLines() {
        List<String> lines = List.of(
                "id,amount",
                "1,2.00",
                "",
                "2,3.00"
        );

        assertEquals(5.00, SalesTotal.sumAmountColumn(lines), 0.0001);
    }

    @Test
    void throwsWhenAmountColumnMissing() {
        List<String> lines = List.of("id,note", "1,a");

        assertThrows(IllegalArgumentException.class, () -> SalesTotal.sumAmountColumn(lines));
    }
}
```

`cleaned: sumAmountColumn/findAmountColumnIndex/parseAmount split by responsibility, path/delimiter/column-name named as constants, header-lookup guarded with a clear exception → safe to change because the parsing logic is unit-testable independent of file I/O, and adding/reordering CSV columns won't break the sum.`