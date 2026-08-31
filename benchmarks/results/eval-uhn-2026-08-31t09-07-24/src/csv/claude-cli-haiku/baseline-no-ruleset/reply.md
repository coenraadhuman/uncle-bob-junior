Here's a complete Java solution:

**Assumptions:**
- The CSV file has headers with an 'amount' column
- The file is located at `sales.csv` in the current directory
- Amount values are numeric (integers or decimals)

```java
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SalesSum {
    public static void main(String[] args) {
        String filePath = "sales.csv";
        double sum = 0;
        int amountColumnIndex = -1;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isHeader = true;
            
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                
                if (isHeader) {
                    // Find the index of the 'amount' column
                    for (int i = 0; i < parts.length; i++) {
                        if (parts[i].trim().equalsIgnoreCase("amount")) {
                            amountColumnIndex = i;
                            break;
                        }
                    }
                    isHeader = false;
                } else {
                    // Sum the amount values
                    if (amountColumnIndex >= 0 && amountColumnIndex < parts.length) {
                        try {
                            double amount = Double.parseDouble(parts[amountColumnIndex].trim());
                            sum += amount;
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid number: " + parts[amountColumnIndex]);
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

This reads the CSV file, locates the 'amount' column, parses each value, and prints the sum. It includes error handling for missing files and invalid numbers.