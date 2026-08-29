Assumptions: `sales.csv` has a header row, is comma-delimited, uses no quoted/escaped commas, and the amount values parse as `double`. Sum logic is extracted from file I/O so it's testable without a file on disk.

```java
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SalesTotal {

    private static final String CSV_DELIMITER = ",";
    private static final String AMOUNT_COLUMN_NAME = "amount";
    private static final String SALES_CSV_PATH = "sales.csv";

    private SalesTotal() {
    }

    public static void main(String[] args) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(Path.of(SALES_CSV_PATH))) {
            double total = sumAmountColumn(reader);
            System.out.println(total);
        }
    }

    static double sumAmountColumn(BufferedReader reader) throws IOException {
        String headerLine = reader.readLine();
        if (headerLine == null) {
            return 0.0;
        }

        int amountColumnIndex = findColumnIndex(headerLine, AMOUNT_COLUMN_NAME);
        double total = 0.0;
        String line;
        while ((line = reader.readLine()) != null) {
            total += parseAmount(line, amountColumnIndex);
        }
        return total;
    }

    private static int findColumnIndex(String headerLine, String columnName) {
        String[] columns = headerLine.split(CSV_DELIMITER);
        for (int i = 0; i < columns.length; i++) {
            if (columns[i].trim().equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Column '" + columnName + "' not found in header");
    }

    private static double parseAmount(String line, int amountColumnIndex) {
        String[] fields = line.split(CSV_DELIMITER);
        return Double.parseDouble(fields[amountColumnIndex].trim());
    }
}
```

```java
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
```

`sumAmountColumn` → cleaned: header-driven column lookup instead of assuming position, guard for empty input, tests cover header-only, empty, reordered, and missing-column cases, safe to change because the sum logic no longer depends on file I/O or column order.