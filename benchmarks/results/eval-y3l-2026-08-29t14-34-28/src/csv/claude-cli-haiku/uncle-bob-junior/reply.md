**Assumptions:**
- CSV file is in the current working directory with headers in the first row
- 'amount' column contains numeric values; invalid entries are skipped
- Simple CSV format without quoted fields
- Tests use JUnit 5

**Main program:**

```java
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class SalesAnalyzer {
    private static final String AMOUNT_COLUMN = "amount";
    
    public static void main(String[] args) throws IOException {
        double sum = calculateSalesSum(Path.of("sales.csv"));
        System.out.println("Total: " + sum);
    }
    
    static double calculateSalesSum(Path csvFile) throws IOException {
        List<String> lines = Files.readAllLines(csvFile);
        if (lines.isEmpty()) {
            return 0.0;
        }
        
        int amountColumnIndex = findColumnIndex(lines.get(0));
        return sumAmountColumn(lines, amountColumnIndex);
    }
    
    private static int findColumnIndex(String headerLine) {
        String[] headers = headerLine.split(",");
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].trim().equalsIgnoreCase(AMOUNT_COLUMN)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Column '" + AMOUNT_COLUMN + "' not found");
    }
    
    private static double sumAmountColumn(List<String> lines, int columnIndex) {
        double sum = 0.0;
        for (int i = 1; i < lines.size(); i++) {
            String[] values = lines.get(i).split(",");
            if (columnIndex < values.length) {
                try {
                    sum += Double.parseDouble(values[columnIndex].trim());
                } catch (NumberFormatException e) {
                    // Skip rows with invalid amounts
                }
            }
        }
        return sum;
    }
}
```

**Tests:**

```java
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

public class SalesAnalyzerTest {
    @Test
    void sumsSalesCorrectly(@TempDir Path tempDir) throws IOException {
        Path csvFile = tempDir.resolve("sales.csv");
        Files.write(csvFile, "amount,date\n10.5,2026-01-01\n20.3,2026-01-02\n15.2,2026-01-03\n".getBytes());
        
        double sum = SalesAnalyzer.calculateSalesSum(csvFile);
        
        assertEquals(46.0, sum, 0.01);
    }
    
    @Test
    void handlesEmptyFile(@TempDir Path tempDir) throws IOException {
        Path csvFile = tempDir.resolve("sales.csv");
        Files.write(csvFile, "amount\n".getBytes());
        
        double sum = SalesAnalyzer.calculateSalesSum(csvFile);
        
        assertEquals(0.0, sum);
    }
    
    @Test
    void skipsInvalidAmounts(@TempDir Path tempDir) throws IOException {
        Path csvFile = tempDir.resolve("sales.csv");
        Files.write(csvFile, "amount\n10\ninvalid\n20\n".getBytes());
        
        double sum = SalesAnalyzer.calculateSalesSum(csvFile);
        
        assertEquals(30.0, sum);
    }
    
    @Test
    void throwsWhenColumnMissing(@TempDir Path tempDir) throws IOException {
        Path csvFile = tempDir.resolve("sales.csv");
        Files.write(csvFile, "price,date\n10,2026-01-01\n".getBytes());
        
        assertThrows(IllegalArgumentException.class, () -> SalesAnalyzer.calculateSalesSum(csvFile));
    }
}
```

→ cleaned: single responsibility per method (`findColumnIndex`, `sumAmountColumn`), guard clause for empty file, named constant for column name; safe to change because behavior is proven by tests covering happy path, empty data, invalid entries, and missing column.