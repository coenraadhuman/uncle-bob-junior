```java
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SalesAnalyzer {
    private static final String CSV_FILE = "sales.csv";
    
    public static void main(String[] args) {
        try {
            double total = calculateTotalAmount();
            System.out.println("Total amount: " + total);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
    
    private static double calculateTotalAmount() throws IOException {
        double total = 0.0;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(CSV_FILE))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IOException("CSV file is empty");
            }
            
            int amountColumnIndex = findColumnIndex(headerLine, "amount");
            if (amountColumnIndex == -1) {
                throw new IOException("Column 'amount' not found in CSV");
            }
            
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields.length > amountColumnIndex) {
                    try {
                        total += Double.parseDouble(fields[amountColumnIndex].trim());
                    } catch (NumberFormatException e) {
                        System.err.println("Skipping invalid amount: " + fields[amountColumnIndex]);
                    }
                }
            }
        }
        
        return total;
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

**Assumptions:** CSV file named `sales.csv` in the current directory, first line is headers, "amount" column contains numeric values (case-insensitive match), comma-delimited with optional whitespace. Invalid rows are skipped with a warning.