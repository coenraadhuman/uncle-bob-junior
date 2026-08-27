```java
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SalesSummarizer {
    public static void main(String[] args) {
        String filePath = "sales.csv";
        double sum = 0.0;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            int amountColumnIndex = -1;
            
            // Read header line to find amount column
            if ((line = reader.readLine()) != null) {
                String[] headers = line.split(",");
                for (int i = 0; i < headers.length; i++) {
                    if (headers[i].trim().equalsIgnoreCase("amount")) {
                        amountColumnIndex = i;
                        break;
                    }
                }
            }
            
            if (amountColumnIndex == -1) {
                System.out.println("Error: 'amount' column not found");
                return;
            }
            
            // Read data lines and sum the amount column
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length > amountColumnIndex) {
                    try {
                        sum += Double.parseDouble(values[amountColumnIndex].trim());
                    } catch (NumberFormatException e) {
                        System.out.println("Warning: Could not parse value: " + values[amountColumnIndex]);
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
- The CSV file is in the current directory as `sales.csv`
- The file has a header row with column names
- The 'amount' column contains numeric values (double)
- Values are comma-separated with no quotes around fields

The program reads the header to locate the 'amount' column, then iterates through each data row, parsing and summing the values. It includes basic error handling for missing columns and unparseable values.