I'll write a Java program that reads `sales.csv` and sums the 'amount' column. I'm assuming the CSV has a header row with column names, and the amounts are numeric values (decimals or integers).

```java
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class SalesSum {
    public static void main(String[] args) {
        String filePath = "sales.csv";
        double total = 0;
        
        try (Stream<String> lines = Files.lines(Paths.get(filePath))) {
            total = lines.skip(1)
                .mapToDouble(line -> {
                    String[] fields = line.split(",");
                    return Double.parseDouble(fields[1]);
                })
                .sum();
        } catch (Exception e) {
            System.err.println("Error reading file: " + e.getMessage());
            return;
        }
        
        System.out.println("Total amount: " + total);
    }
}
```

This solution:
- Reads the CSV file line by line using `Files.lines()`
- Skips the first line (header row)
- Splits each line by comma and extracts the second field (assuming 'amount' is the second column)
- Converts values to `double` and sums them
- Prints the total

If your CSV has a different column order, replace `fields[1]` with the correct index, or modify it to find the 'amount' column by header name for a more robust approach.