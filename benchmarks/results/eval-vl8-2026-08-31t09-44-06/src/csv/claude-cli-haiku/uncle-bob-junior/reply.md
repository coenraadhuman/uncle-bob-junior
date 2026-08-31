```java
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SalesCalculator {
    private static final String AMOUNT_HEADER = "amount";
    private static final String CSV_FILE = "sales.csv";
    
    public static void main(String[] args) {
        try {
            double total = calculateTotalSales(CSV_FILE);
            System.out.println("Total sales: " + total);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid CSV format: " + e.getMessage());
        }
    }
    
    public static double calculateTotalSales(String filename) throws IOException {
        var lines = Files.readAllLines(Paths.get(filename));
        if (lines.isEmpty()) {
            return 0.0;
        }
        
        var headers = lines.get(0).split(",");
        int amountColumnIndex = findColumnIndex(headers, AMOUNT_HEADER);
        if (amountColumnIndex < 0) {
            throw new IllegalArgumentException("Column 'amount' not found");
        }
        
        return lines.stream()
            .skip(1)
            .mapToDouble(line -> parseAmount(line.split(","), amountColumnIndex))
            .sum();
    }
    
    private static int findColumnIndex(String[] headers, String columnName) {
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].trim().equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        return -1;
    }
    
    private static double parseAmount(String[] fields, int columnIndex) {
        if (columnIndex >= fields.length) {
            return 0.0;
        }
        try {
            return Double.parseDouble(fields[columnIndex].trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
```

```java
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

public class SalesCalculatorTest {
    
    @Test
    void calculatesCorrectSum(@TempDir Path tempDir) throws IOException {
        Path csvFile = tempDir.resolve("sales.csv");
        Files.write(csvFile, "product,amount\nWidget,100.50\nGadget,200.25\n".getBytes());
        
        double result = SalesCalculator.calculateTotalSales(csvFile.toString());
        
        assertEquals(300.75, result, 0.01);
    }
    
    @Test
    void handlesEmptyFile(@TempDir Path tempDir) throws IOException {
        Path csvFile = tempDir.resolve("sales.csv");
        Files.write(csvFile, "amount\n".getBytes());
        
        double result = SalesCalculator.calculateTotalSales(csvFile.toString());
        
        assertEquals(0.0, result);
    }
    
    @Test
    void throwsWhenAmountColumnMissing(@TempDir Path tempDir) throws IOException {
        Path csvFile = tempDir.resolve("sales.csv");
        Files.write(csvFile, "product,price\nWidget,100\n".getBytes());
        
        assertThrows(IllegalArgumentException.class, 
            () -> SalesCalculator.calculateTotalSales(csvFile.toString()));
    }
    
    @Test
    void skipsInvalidNumbers(@TempDir Path tempDir) throws IOException {
        Path csvFile = tempDir.resolve("sales.csv");
        Files.write(csvFile, "amount\n100.50\ninvalid\n200.25\n".getBytes());
        
        double result = SalesCalculator.calculateTotalSales(csvFile.toString());
        
        assertEquals(300.75, result, 0.01);
    }
}
```

**Assumptions:** CSV has a header row; 'amount' column holds decimal numbers; simple comma-separated format (no quoted fields); invalid values treated as 0.

**Cleaned:** One function per responsibility (`findColumnIndex`, `parseAmount`), guard clauses for empty/invalid cases, named constants for column name and filename, defensive parsing with null/parse checks. Safe to change because each function does one thing and error cases are handled explicitly.