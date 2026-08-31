I'll write a clean Java program that reads a CSV file and sums the 'amount' column.

**Assumptions:**
- CSV has headers in the first row with an "amount" column
- Amount values are numeric; invalid entries are skipped
- Standard comma-delimited format

```java
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SalesAnalyzer {
    private static final String CSV_FILE = "sales.csv";
    private static final String DELIMITER = ",";

    public static void main(String[] args) {
        try {
            double total = sumAmountColumn(CSV_FILE);
            System.out.println("Total amount: " + total);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    static double sumAmountColumn(String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return 0.0;
            }

            int amountColumnIndex = findAmountColumnIndex(headerLine);
            if (amountColumnIndex == -1) {
                throw new IllegalArgumentException("'amount' column not found in CSV header");
            }

            double sum = 0.0;
            String line;
            while ((line = reader.readLine()) != null) {
                sum += parseAmount(line, amountColumnIndex);
            }
            return sum;
        }
    }

    private static int findAmountColumnIndex(String headerLine) {
        String[] headers = headerLine.split(DELIMITER);
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].trim().equalsIgnoreCase("amount")) {
                return i;
            }
        }
        return -1;
    }

    private static double parseAmount(String line, int columnIndex) {
        String[] values = line.split(DELIMITER);
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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class SalesAnalyzerTest {
    public static void main(String[] args) throws IOException {
        testSumWithValidCsv();
        testSumWithEmptyFile();
        testSumWithMissingAmountColumn();
        testSumWithInvalidNumbers();
        System.out.println("All tests passed!");
    }

    private static void testSumWithValidCsv() throws IOException {
        Path tempFile = Files.createTempFile("test_sales_", ".csv");
        try {
            String csv = "id,product,amount\n1,Widget,100.50\n2,Gadget,200.75\n3,Tool,50.25";
            Files.writeString(tempFile, csv);
            double sum = SalesAnalyzer.sumAmountColumn(tempFile.toString());
            assert sum == 351.50 : "Expected 351.50, got " + sum;
        } finally {
            Files.delete(tempFile);
        }
    }

    private static void testSumWithEmptyFile() throws IOException {
        Path tempFile = Files.createTempFile("test_sales_", ".csv");
        try {
            Files.writeString(tempFile, "");
            double sum = SalesAnalyzer.sumAmountColumn(tempFile.toString());
            assert sum == 0.0 : "Expected 0.0 for empty file";
        } finally {
            Files.delete(tempFile);
        }
    }

    private static void testSumWithMissingAmountColumn() throws IOException {
        Path tempFile = Files.createTempFile("test_sales_", ".csv");
        try {
            String csv = "id,product\n1,Widget";
            Files.writeString(tempFile, csv);
            try {
                SalesAnalyzer.sumAmountColumn(tempFile.toString());
                assert false : "Should throw IllegalArgumentException";
            } catch (IllegalArgumentException e) {
                assert e.getMessage().contains("'amount' column not found");
            }
        } finally {
            Files.delete(tempFile);
        }
    }

    private static void testSumWithInvalidNumbers() throws IOException {
        Path tempFile = Files.createTempFile("test_sales_", ".csv");
        try {
            String csv = "id,product,amount\n1,Widget,100.50\n2,Gadget,invalid\n3,Tool,50.25";
            Files.writeString(tempFile, csv);
            double sum = SalesAnalyzer.sumAmountColumn(tempFile.toString());
            assert sum == 150.75 : "Expected 150.75 (skipping invalid)";
        } finally {
            Files.delete(tempFile);
        }
    }
}
```

Cleaned: separated parsing logic into focused functions—`findAmountColumnIndex`, `parseAmount`—with guard clauses and early returns; safe to change because each function has one responsibility and tests cover valid CSV, empty file, missing column, and invalid amounts.