**Assumptions:**
- sales.csv is in the current working directory
- CSV has headers in the first row with 'amount' column (case-insensitive)
- Amount values are numeric (double-compatible)
- Simple CSV format (no embedded commas in quoted fields)
- Invalid or missing amounts are treated as zero

```java
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class SalesAnalyzer {
    
    public static double sumAmountColumn(String filePath) throws FileNotFoundException {
        double sum = 0.0;
        
        try (Scanner scanner = new Scanner(new File(filePath))) {
            if (!scanner.hasNextLine()) {
                return 0.0;
            }
            
            String headerLine = scanner.nextLine();
            int amountColumnIndex = findColumnIndex(headerLine, "amount");
            
            if (amountColumnIndex == -1) {
                throw new IllegalArgumentException("'amount' column not found");
            }
            
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                sum += extractAmount(line, amountColumnIndex);
            }
        }
        
        return sum;
    }
    
    private static double extractAmount(String line, int columnIndex) {
        if (line.trim().isEmpty()) {
            return 0.0;
        }
        
        String[] fields = line.split(",");
        if (columnIndex >= fields.length) {
            return 0.0;
        }
        
        try {
            return Double.parseDouble(fields[columnIndex].trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
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
    
    public static void main(String[] args) {
        try {
            double total = sumAmountColumn("sales.csv");
            System.out.println(total);
        } catch (FileNotFoundException e) {
            System.err.println("Error: File not found");
            System.exit(1);
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }
}
```

Cleaned: one responsibility per method (summing, extraction, column lookup); guard clauses for empty and invalid cases; named helper methods reveal intent; testable structure with no side effects in core logic.