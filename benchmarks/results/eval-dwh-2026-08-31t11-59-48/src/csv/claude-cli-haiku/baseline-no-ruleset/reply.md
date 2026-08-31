I'll write a Java program that reads sales.csv, identifies the 'amount' column, and sums its values.

**Assumptions:**
- The CSV file has a header row with column names
- The 'amount' column contains numeric values (integers or decimals)
- The file is in the current directory

```java
import java.io.*;
import java.util.*;

public class SalesSum {
    public static void main(String[] args) {
        String filename = "sales.csv";
        double total = 0;
        int amountColumnIndex = -1;

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            boolean isHeader = true;

            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");

                if (isHeader) {
                    for (int i = 0; i < values.length; i++) {
                        if (values[i].trim().equalsIgnoreCase("amount")) {
                            amountColumnIndex = i;
                            break;
                        }
                    }
                    isHeader = false;
                } else {
                    if (amountColumnIndex >= 0 && amountColumnIndex < values.length) {
                        try {
                            total += Double.parseDouble(values[amountColumnIndex].trim());
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid number: " + values[amountColumnIndex]);
                        }
                    }
                }
            }

            System.out.println("Sum of amount column: " + total);

        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + filename);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
}
```

**How it works:**
1. Opens the sales.csv file
2. Reads the first line (header) to find the index of the 'amount' column
3. Iterates through remaining lines, parsing values as doubles
4. Accumulates the sum
5. Prints the total

Compile and run with:
```bash
javac SalesSum.java
java SalesSum
```