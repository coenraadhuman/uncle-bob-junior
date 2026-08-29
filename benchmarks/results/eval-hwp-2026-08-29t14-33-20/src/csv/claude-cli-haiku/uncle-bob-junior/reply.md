I'll write a clean Java program to read sales.csv and sum the 'amount' column.

**Assumptions:**
- sales.csv has a header row with an "amount" column
- Amount values are valid decimal numbers
- File is in the current working directory

```java
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SalesAnalyzer {
    private static final String DEFAULT_FILE_PATH = "sales.csv";
    private static final String AMOUNT_COLUMN_NAME = "amount";

    public static void main(String[] args) {
        String filePath = args.length > 0 ? args[0] : DEFAULT_FILE_PATH;
        double total = sumAmountColumn(filePath);
        System.out.println("Total amount: " + total);
    }

    static double sumAmountColumn(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IllegalArgumentException("CSV file is empty");
            }

            int amountColumnIndex = findColumnIndex(headerLine, AMOUNT_COLUMN_NAME);
            if (amountColumnIndex == -1) {
                throw new IllegalArgumentException("Column '" + AMOUNT_COLUMN_NAME + "' not found");
            }

            return sumColumn(reader, amountColumnIndex);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: " + filePath, e);
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
            double value = parseAmount(line, columnIndex);
            sum += value;
        }
        return sum;
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

**Tests:**

```java
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class SalesAnalyzerTest {
    @Test
    void sumAmountColumn_withValidData_returnTotal(@TempDir File tempDir) throws IOException {
        File csvFile = new File(tempDir, "sales.csv");
        try (FileWriter writer = new FileWriter(csvFile)) {
            writer.write("id,amount,date\n");
            writer.write("1,100.50,2026-01-01\n");
            writer.write("2,200.75,2026-01-02\n");
            writer.write("3,150.25,2026-01-03\n");
        }

        double result = SalesAnalyzer.sumAmountColumn(csvFile.getAbsolutePath());
        assertEquals(451.50, result, 0.01);
    }

    @Test
    void sumAmountColumn_withInvalidAmounts_skipAndSum(@TempDir File tempDir) throws IOException {
        File csvFile = new File(tempDir, "sales.csv");
        try (FileWriter writer = new FileWriter(csvFile)) {
            writer.write("amount\n");
            writer.write("100\n");
            writer.write("invalid\n");
            writer.write("200\n");
        }

        double result = SalesAnalyzer.sumAmountColumn(csvFile.getAbsolutePath());
        assertEquals(300.0, result, 0.01);
    }

    @Test
    void sumAmountColumn_missingColumn_throwsException(@TempDir File tempDir) throws IOException {
        File csvFile = new File(tempDir, "sales.csv");
        try (FileWriter writer = new FileWriter(csvFile)) {
            writer.write("id,value\n");
            writer.write("1,100\n");
        }

        assertThrows(IllegalArgumentException.class,
            () -> SalesAnalyzer.sumAmountColumn(csvFile.getAbsolutePath()));
    }

    @Test
    void sumAmountColumn_emptyFile_throwsException(@TempDir File tempDir) throws IOException {
        File csvFile = new File(tempDir, "sales.csv");
        csvFile.createNewFile();

        assertThrows(IllegalArgumentException.class,
            () -> SalesAnalyzer.sumAmountColumn(csvFile.getAbsolutePath()));
    }
}
```

→ cleaned: one function per responsibility (column finding, parsing, summing), guard clauses for invalid states, named constants for file paths and column names; safe to change because tests cover the happy path and edge cases (missing column, invalid amounts, empty file).