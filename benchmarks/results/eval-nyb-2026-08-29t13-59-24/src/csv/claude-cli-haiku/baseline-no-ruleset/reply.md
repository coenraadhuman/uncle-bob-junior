I'll write a Java program that reads a CSV file and sums the 'amount' column.

**Assumptions:**
- The CSV has a header row with 'amount' as a column name
- The 'amount' column contains numeric values (double)
- The file is in the current directory

```java
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SalesSum {
    public static void main(String[] args) {
        double total = 0;
        int amountColumnIndex = -1;
        
        try (BufferedReader reader = new BufferedReader(new FileReader("sales.csv"))) {
            String line;
            boolean isFirstLine = true;
            
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                
                // Find the 'amount' column index from the header
                if (isFirstLine) {
                    for (int i = 0; i < parts.length; i++) {
                        if (parts[i].trim().equalsIgnoreCase("amount")) {
                            amountColumnIndex = i;
                            break;
                        }
                    }
                    isFirstLine = false;
                    continue;
                }
                
                // Sum the amount column
                if (amountColumnIndex >= 0 && amountColumnIndex < parts.length) {
                    try {
                        total += Double.parseDouble(parts[amountColumnIndex].trim());
                    } catch (NumberFormatException e) {
                        System.err.println("Warning: Could not parse value: " + parts[amountColumnIndex]);
                    }
                }
            }
            
            System.out.println("Total: " + total);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
}
```

To run it, compile and execute:
```bash
javac SalesSum.java
java SalesSum
```

The program reads the CSV line by line, identifies the 'amount' column from the header row, and accumulates the sum of all numeric values in that column.