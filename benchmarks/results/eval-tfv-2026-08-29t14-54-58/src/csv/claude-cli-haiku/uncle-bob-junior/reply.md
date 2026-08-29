I'll write a complete Java program with tests. 

**Assumptions:**
- CSV file has a header row with column names
- 'amount' column exists and contains numeric values (doubles)
- Simple CSV format (comma-separated, no quoted fields with embedded commas)
- Column names are case-insensitive
- Invalid amount values are skipped

```java
import java.io.*;

public class SalesAnalyzer {
    private static final String AMOUNT_COLUMN = "amount";
    private static final int NOT_FOUND = -1;

    public static void main(String[] args) {
        String filePath = "sales.csv";
        try {
            double totalAmount = calculateTotalAmount(filePath);
            System.out.println("Total amount: " + totalAmount);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public static double calculateTotalAmount(String filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return 0.0;
            }

            int amountColumnIndex = findColumnIndex(headerLine, AMOUNT_COLUMN);
            if (amountColumnIndex == NOT_FOUND) {
                throw new IllegalArgumentException("Column '" + AMOUNT_COLUMN + "' not found");
            }

            double total = 0.0;
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    total += parseAmount(line, amountColumnIndex);
                }
            }
            return total;
        }
    }

    private static int findColumnIndex(String headerLine, String columnName) {
        String[] headers = headerLine.split(",");
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].trim().equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        return NOT_FOUND;
    }

    private static double parseAmount(String line, int columnIndex) {
        String[] values = line.split(",");
        if (columnIndex >= values.length) {
            return 0.0;
        }
        try {
            return Double.parseDouble(values[columnIndex].trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
```

```java
import java.io.*;
import java.nio.file.*;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class SalesAnalyzerTest {
    private static final String TEST_FILE = "test_sales.csv";

    @Test
    public void testSumValidAmounts() throws IOException {
        writeCsv("id,name,amount\n1,Product A,100.50\n2,Product B,200.75\n");
        assertEquals(301.25, SalesAnalyzer.calculateTotalAmount(TEST_FILE), 0.01);
    }

    @Test
    public void testSumWithEmptyLines() throws IOException {
        writeCsv("id,name,amount\n1,Product A,100.50\n\n2,Product B,50.00\n");
        assertEquals(150.50, SalesAnalyzer.calculateTotalAmount(TEST_FILE), 0.01);
    }

    @Test
    public void testIgnoresInvalidAmountValues() throws IOException {
        writeCsv("id,name,amount\n1,Product A,100.50\n2,Product B,invalid\n3,Product C,50.00\n");
        assertEquals(150.50, SalesAnalyzer.calculateTotalAmount(TEST_FILE), 0.01);
    }

    @Test
    public void testEmptyFileReturnsZero() throws IOException {
        writeCsv("");
        assertEquals(0.0, SalesAnalyzer.calculateTotalAmount(TEST_FILE));
    }

    @Test
    public void testHeaderOnlyReturnsZero() throws IOException {
        writeCsv("id,name,amount\n");
        assertEquals(0.0, SalesAnalyzer.calculateTotalAmount(TEST_FILE));
    }

    @Test
    public void testMissingAmountColumnThrows() throws IOException {
        writeCsv("id,name,price\n1,Product A,100.50\n");
        assertThrows(IllegalArgumentException.class, 
                     () -> SalesAnalyzer.calculateTotalAmount(TEST_FILE));
    }

    @Test
    public void testCaseInsensitiveColumnName() throws IOException {
        writeCsv("id,name,AMOUNT\n1,Product A,75.25\n");
        assertEquals(75.25, SalesAnalyzer.calculateTotalAmount(TEST_FILE), 0.01);
    }

    private void writeCsv(String content) throws IOException {
        Files.write(Paths.get(TEST_FILE), content.getBytes());
    }
}
```

Cleaned: header and column lookup separated, invalid amounts skipped gracefully, early return on empty file. Safe to change because single-responsibility functions let you modify parsing logic or error handling without affecting the sum loop.