```java
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SalesSum {
    public static void main(String[] args) {
        String filePath = "sales.csv";
        double totalAmount = 0.0;
        int amountColumnIndex = -1;

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isHeader = true;

            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");

                // Find the 'amount' column index from header
                if (isHeader) {
                    for (int i = 0; i < values.length; i++) {
                        if (values[i].trim().equalsIgnoreCase("amount")) {
                            amountColumnIndex = i;
                            break;
                        }
                    }
                    if (amountColumnIndex == -1) {
                        System.err.println("Error: 'amount' column not found");
                        return;
                    }
                    isHeader = false;
                    continue;
                }

                // Parse and sum the amount column
                try {
                    double amount = Double.parseDouble(values[amountColumnIndex].trim());
                    totalAmount += amount;
                } catch (NumberFormatException e) {
                    System.err.println("Warning: Invalid number skipped: " + values[amountColumnIndex]);
                }
            }

            System.out.println("Total Amount: " + totalAmount);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
}
```

**Assumptions:**
- The CSV file is named `sales.csv` in the working directory
- The first row contains headers with an "amount" column (case-insensitive)
- Amount values are numeric (integers or decimals)
- Invalid numbers are logged as warnings but don't stop processing

To compile and run:
```bash
javac SalesSum.java
java SalesSum
```