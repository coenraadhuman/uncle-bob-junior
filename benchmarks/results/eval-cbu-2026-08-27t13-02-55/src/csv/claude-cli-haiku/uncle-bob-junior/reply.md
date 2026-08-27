```java
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SalesSummary {
    public static void main(String[] args) {
        try {
            double totalAmount = calculateSalesTotal("sales.csv");
            System.out.println("Total Amount: " + totalAmount);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Error parsing amount values: " + e.getMessage());
        }
    }

    private static double calculateSalesTotal(String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IOException("File is empty");
            }

            int amountColumnIndex = findColumnIndex(headerLine, "amount");
            if (amountColumnIndex == -1) {
                throw new IOException("'amount' column not found in CSV header");
            }

            double total = 0.0;
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                if (amountColumnIndex < values.length) {
                    total += Double.parseDouble(values[amountColumnIndex].trim());
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
        return -1;
    }
}
```

**Assumptions:**
- `sales.csv` is in the current working directory
- The file has a header row with an 'amount' column (case-insensitive)
- Amount values are valid numbers (integers or decimals)
- CSV uses comma as delimiter with no quoted fields

**Compile and run:**
```bash
javac SalesSummary.java
java SalesSummary
```