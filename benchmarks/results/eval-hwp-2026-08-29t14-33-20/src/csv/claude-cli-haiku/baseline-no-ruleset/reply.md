```java
import java.io.*;

public class SalesSum {
    public static void main(String[] args) {
        String filePath = "sales.csv";
        double sum = 0;
        int amountColumnIndex = -1;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isHeader = true;
            
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                
                if (isHeader) {
                    // Find the index of 'amount' column
                    for (int i = 0; i < values.length; i++) {
                        if (values[i].trim().equalsIgnoreCase("amount")) {
                            amountColumnIndex = i;
                            break;
                        }
                    }
                    isHeader = false;
                } else {
                    // Sum values from amount column
                    if (amountColumnIndex >= 0 && amountColumnIndex < values.length) {
                        try {
                            sum += Double.parseDouble(values[amountColumnIndex].trim());
                        } catch (NumberFormatException e) {
                            // Skip non-numeric values
                        }
                    }
                }
            }
            
            System.out.println("Sum of amount column: " + sum);
            
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
}
```

**Assumptions:**
- The CSV has a header row with an "amount" column (case-insensitive)
- The file is named `sales.csv` in the current directory
- Amount values are numeric (doubles); non-numeric rows are skipped
- Simple comma-separated format without quoted fields containing commas

Compile and run with:
```bash
javac SalesSum.java
java SalesSum
```