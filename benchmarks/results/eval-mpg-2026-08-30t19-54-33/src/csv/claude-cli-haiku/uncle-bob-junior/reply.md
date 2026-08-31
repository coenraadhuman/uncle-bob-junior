I'll write a clean Java program that reads sales.csv and sums the 'amount' column.

**Assumptions:**
- sales.csv exists in the current directory
- CSV has a header row with an "amount" column (case-insensitive)
- Amount values are valid numbers (doubles)
- Standard comma-separated format

```java
import java.io.*;
import java.nio.file.*;

public class SalesCalculator {
    private static final String AMOUNT_COLUMN = "amount";

    public static void main(String[] args) throws IOException {
        double total = sumAmountsFromFile(Paths.get("sales.csv"));
        System.out.println(total);
    }

    public static double sumAmountsFromFile(Path filePath) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IllegalArgumentException("CSV file is empty");
            }

            int amountIndex = findColumnIndex(headerLine, AMOUNT_COLUMN);
            if (amountIndex == -1) {
                throw new IllegalArgumentException("Column '" + AMOUNT_COLUMN + "' not found");
            }

            return sumColumn(reader, amountIndex);
        }
    }

    private static int findColumnIndex(String headerLine, String columnName) {
        String[] headers = headerLine.split(",");
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].trim().equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        return -1;
    }

    private static double sumColumn(BufferedReader reader, int columnIndex) throws IOException {
        double sum = 0.0;
        String line;
        while ((line = reader.readLine()) != null) {
            String[] fields = line.split(",");
            if (fields.length > columnIndex) {
                String value = fields[columnIndex].trim();
                if (!value.isEmpty()) {
                    sum += Double.parseDouble(value);
                }
            }
        }
        return sum;
    }
}
```

```java
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.*;

import static org.junit.jupiter.api.Assertions.*;

public class SalesCalculatorTest {
    @TempDir
    Path tempDir;

    @Test
    void sumsSalesAmounts() throws IOException {
        Path csv = tempDir.resolve("test.csv");
        Files.writeString(csv, "amount\n10.50\n20.75\n30.25");
        assertEquals(61.50, SalesCalculator.sumAmountsFromFile(csv), 0.01);
    }

    @Test
    void handlesMultipleColumns() throws IOException {
        Path csv = tempDir.resolve("test.csv");
        Files.writeString(csv, "name,amount,date\nWidget,100.00,2026-01-01\nGadget,50.50,2026-01-02");
        assertEquals(150.50, SalesCalculator.sumAmountsFromFile(csv), 0.01);
    }

    @Test
    void caseInsensitiveColumnName() throws IOException {
        Path csv = tempDir.resolve("test.csv");
        Files.writeString(csv, "Amount\n100\n200");
        assertEquals(300.0, SalesCalculator.sumAmountsFromFile(csv), 0.01);
    }

    @Test
    void throwsWhenColumnNotFound() throws IOException {
        Path csv = tempDir.resolve("test.csv");
        Files.writeString(csv, "value\n100");
        assertThrows(IllegalArgumentException.class, () -> SalesCalculator.sumAmountsFromFile(csv));
    }

    @Test
    void throwsOnEmptyFile() throws IOException {
        Path csv = tempDir.resolve("test.csv");
        Files.writeString(csv, "");
        assertThrows(IllegalArgumentException.class, () -> SalesCalculator.sumAmountsFromFile(csv));
    }
}
```

**Implementation:** Finds the "amount" column in the header, iterates rows, parses values, and accumulates the sum. Guard clauses handle empty files and missing columns; empty values in rows are skipped. Tests verify the happy path, edge cases, and error conditions.