I'll write a Java program that reads `sales.csv`, finds the 'amount' column by header name, and sums its values. I'm assuming the CSV has a header row and the 'amount' column contains numeric values.

```java
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class SalesProcessor {
    public static void main(String[] args) throws IOException {
        double total = sumAmountsFromCsv("sales.csv");
        System.out.println("Total: " + total);
    }
    
    static double sumAmountsFromCsv(String filePath) throws IOException {
        List<String> lines = Files.readAllLines(Path.of(filePath));
        return sumAmounts(lines);
    }
    
    static double sumAmounts(List<String> lines) {
        if (lines.isEmpty()) {
            return 0.0;
        }
        
        int amountColumnIndex = findColumnIndex(lines.get(0), "amount");
        
        return lines.stream()
                    .skip(1)
                    .mapToDouble(line -> parseAmount(line, amountColumnIndex))
                    .sum();
    }
    
    private static int findColumnIndex(String headerLine, String columnName) {
        String[] headers = headerLine.split(",");
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].trim().equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Column not found: " + columnName);
    }
    
    private static double parseAmount(String line, int columnIndex) {
        String[] fields = line.split(",");
        return Double.parseDouble(fields[columnIndex].trim());
    }
}
```

```java
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SalesProcessorTest {
    @Test
    void sumAmountsWithValidData() {
        List<String> lines = Arrays.asList(
            "product,amount",
            "Item1,10.5",
            "Item2,20.3",
            "Item3,15.2"
        );
        
        double result = SalesProcessor.sumAmounts(lines);
        
        assertEquals(46.0, result, 0.01);
    }
    
    @Test
    void sumAmountsWithEmptyLines() {
        List<String> lines = Arrays.asList("product,amount");
        
        double result = SalesProcessor.sumAmounts(lines);
        
        assertEquals(0.0, result);
    }
    
    @Test
    void sumAmountsWithAmountInDifferentPosition() {
        List<String> lines = Arrays.asList(
            "id,product,amount,date",
            "1,Item1,10.5,2026-01-01",
            "2,Item2,20.3,2026-01-02"
        );
        
        double result = SalesProcessor.sumAmounts(lines);
        
        assertEquals(30.8, result, 0.01);
    }
}
```

Cleaned: separated file I/O from sum logic for testability; column lookup handles any position and is case-insensitive; safe to change because tests verify behavior across valid data, empty rows, and different column positions.