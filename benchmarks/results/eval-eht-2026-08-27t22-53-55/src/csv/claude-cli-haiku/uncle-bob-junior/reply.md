**Assumptions:**
- CSV file has headers in the first row with an "amount" column (case-insensitive)
- Amount values are numeric (parsed as `double`)
- Simple CSV format without quoted fields containing commas
- File is located at `sales.csv` in the current directory

Here's the solution:

```java
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SalesAnalyzer {
    private static final String CSV_FILE = "sales.csv";
    private static final String AMOUNT_COLUMN_NAME = "amount";

    public static void main(String[] args) {
        double totalSales = calculateTotalSalesAmount(CSV_FILE);
        System.out.println(totalSales);
    }

    public static double calculateTotalSalesAmount(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return 0;
            }
            
            int amountColumnIndex = findColumnIndex(headerLine, AMOUNT_COLUMN_NAME);
            if (amountColumnIndex == -1) {
                throw new IllegalArgumentException("Column '" + AMOUNT_COLUMN_NAME + "' not found in CSV");
            }
            
            return sumColumnValues(reader, amountColumnIndex);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            return 0;
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

    private static double sumColumnValues(BufferedReader reader, int columnIndex) throws IOException {
        double sum = 0;
        String line;
        
        while ((line = reader.readLine()) != null) {
            String[] values = line.split(",");
            if (columnIndex < values.length) {
                sum += parseAmount(values[columnIndex].trim());
            }
        }
        
        return sum;
    }

    private static double parseAmount(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            System.err.println("Warning: skipping invalid amount: " + value);
            return 0;
        }
    }
}
```

Run with `java SalesAnalyzer` in the directory containing `sales.csv`. The program extracts the amount column by name, sums all numeric values, and prints the total.