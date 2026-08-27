I'll write a complete Java program that reads `sales.csv` and sums the 'amount' column.

**Assumptions:**
- The CSV file has a header row with 'amount' as a column name
- The 'amount' values are numeric (double/integer)
- The file is in the current working directory

```java
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SalesSummarizer {
    public static void main(String[] args) {
        String csvFile = "sales.csv";
        double total = 0.0;
        int amountColumnIndex = -1;
        boolean firstLine = true;

        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");

                if (firstLine) {
                    // Find the index of the 'amount' column
                    for (int i = 0; i < values.length; i++) {
                        if (values[i].trim().equalsIgnoreCase("amount")) {
                            amountColumnIndex = i;
                            break;
                        }
                    }
                    if (amountColumnIndex == -1) {
                        System.err.println("Error: 'amount' column not found in CSV");
                        return;
                    }
                    firstLine = false;
                } else {
                    // Parse and sum the amount values
                    if (amountColumnIndex < values.length) {
                        try {
                            double amount = Double.parseDouble(values[amountColumnIndex].trim());
                            total += amount;
                        } catch (NumberFormatException e) {
                            System.err.println("Warning: Could not parse amount value: " + values[amountColumnIndex]);
                        }
                    }
                }
            }
            System.out.println("Total amount: " + total);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
}
```

**To use this program:**
1. Save it as `SalesSummarizer.java`
2. Compile: `javac SalesSummarizer.java`
3. Run: `java SalesSummarizer`

The program reads the CSV, finds the 'amount' column by header name, and prints the sum to the console.