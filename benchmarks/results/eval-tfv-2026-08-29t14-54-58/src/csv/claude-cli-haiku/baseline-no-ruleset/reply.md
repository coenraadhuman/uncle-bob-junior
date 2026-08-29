I'll write a Java program that reads sales.csv and sums the 'amount' column using Java's built-in file reading capabilities.

**Assumptions:**
- The CSV file has a header row with column names
- The 'amount' column contains numeric values (doubles)
- The file is in the current working directory

```java
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SalesSum {
    public static void main(String[] args) {
        String fileName = "sales.csv";
        double total = 0;
        int amountColumnIndex = -1;

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            boolean isHeader = true;

            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");

                if (isHeader) {
                    for (int i = 0; i < fields.length; i++) {
                        if (fields[i].trim().equalsIgnoreCase("amount")) {
                            amountColumnIndex = i;
                            break;
                        }
                    }
                    isHeader = false;
                    continue;
                }

                if (amountColumnIndex != -1 && amountColumnIndex < fields.length) {
                    try {
                        total += Double.parseDouble(fields[amountColumnIndex].trim());
                    } catch (NumberFormatException e) {
                        System.err.println("Warning: Could not parse amount value: " + fields[amountColumnIndex]);
                    }
                }
            }

            System.out.println("Sum of amount column: " + total);

        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
}
```

The program finds the 'amount' column header, iterates through each row, and accumulates the values. It handles parsing errors gracefully and prints the total at the end.