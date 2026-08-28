I'll provide a complete Java program that reads sales.csv and sums the 'amount' column.

**Assumptions:**
- The CSV file has a header row with an "amount" column
- The file is named `sales.csv` in the current directory
- Amount values are numeric (double)
- The file is properly formatted with comma separators

```java
import java.io.*;
import java.util.*;

public class SalesSumCalculator {
    public static void main(String[] args) {
        String filePath = "sales.csv";
        double totalSum = 0.0;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            String[] headers = null;
            int amountIndex = -1;
            
            while ((line = reader.readLine()) != null) {
                if (headers == null) {
                    // Parse header row
                    headers = line.split(",");
                    for (int i = 0; i < headers.length; i++) {
                        if (headers[i].trim().equalsIgnoreCase("amount")) {
                            amountIndex = i;
                            break;
                        }
                    }
                    if (amountIndex == -1) {
                        System.err.println("Error: 'amount' column not found in CSV");
                        return;
                    }
                } else {
                    // Parse data rows
                    String[] values = line.split(",");
                    if (amountIndex < values.length) {
                        try {
                            double amount = Double.parseDouble(values[amountIndex].trim());
                            totalSum += amount;
                        } catch (NumberFormatException e) {
                            System.err.println("Warning: Could not parse amount value: " + values[amountIndex]);
                        }
                    }
                }
            }
            
            System.out.println("Total sum of 'amount' column: " + totalSum);
            
        } catch (FileNotFoundException e) {
            System.err.println("Error: File not found - " + filePath);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
}
```

**To run:**
1. Compile: `javac SalesSumCalculator.java`
2. Execute: `java SalesSumCalculator`

The program handles edge cases like missing 'amount' column, malformed numbers, and file I/O errors.