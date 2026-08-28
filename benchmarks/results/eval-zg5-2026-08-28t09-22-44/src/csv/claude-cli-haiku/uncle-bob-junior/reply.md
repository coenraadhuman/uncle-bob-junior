I'll provide a clean Java solution to read sales.csv and sum the 'amount' column.

**Assumptions:**
- CSV file has a header row with column names (including "amount")
- Column name matching is case-insensitive
- Invalid/unparseable amounts are skipped
- File is in the current directory

```java
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SalesCalculator {
    private static final String AMOUNT_COLUMN = "amount";
    
    public double calculateTotal(String filename) throws IOException {
        double sum = 0.0;
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String header = reader.readLine();
            if (header == null) {
                return 0.0;
            }
            
            int amountIndex = findColumnIndex(header, AMOUNT_COLUMN);
            if (amountIndex == -1) {
                throw new IllegalArgumentException("Column '" + AMOUNT_COLUMN + "' not found");
            }
            
            String line;
            while ((line = reader.readLine()) != null) {
                sum += extractAmount(line, amountIndex);
            }
        }
        return sum;
    }
    
    private int findColumnIndex(String header, String columnName) {
        String[] columns = header.split(",");
        for (int i = 0; i < columns.length; i++) {
            if (columns[i].trim().equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        return -1;
    }
    
    private double extractAmount(String line, int columnIndex) {
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
}
```

```java
public class SalesReader {
    private static final String CSV_FILE = "sales.csv";
    
    public static void main(String[] args) {
        try {
            double total = new SalesCalculator().calculateTotal(CSV_FILE);
            System.out.println("Total: " + total);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            System.exit(1);
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }
}
```

```java
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SalesCalculatorTest {
    public static void main(String[] args) throws IOException {
        testHappyPath();
        testEmptyFile();
        testMissingColumn();
        testSkipsInvalidAmounts();
        System.out.println("All tests passed");
    }
    
    private static void testHappyPath() throws IOException {
        String filename = "test_sales.csv";
        Files.write(Paths.get(filename), "id,amount,date\n1,100.50\n2,200.75\n3,150.25\n".getBytes());
        
        double total = new SalesCalculator().calculateTotal(filename);
        assert Math.abs(total - 451.50) < 0.01 : "Expected 451.50, got " + total;
        new File(filename).delete();
    }
    
    private static void testEmptyFile() throws IOException {
        String filename = "test_empty.csv";
        Files.write(Paths.get(filename), "amount\n".getBytes());
        
        double total = new SalesCalculator().calculateTotal(filename);
        assert total == 0.0 : "Expected 0.0 for file with only header";
        new File(filename).delete();
    }
    
    private static void testMissingColumn() throws IOException {
        String filename = "test_no_col.csv";
        Files.write(Paths.get(filename), "id,name\n1,Test\n".getBytes());
        
        try {
            new SalesCalculator().calculateTotal(filename);
            assert false : "Should throw IllegalArgumentException";
        } catch (IllegalArgumentException e) {
            assert e.getMessage().contains("amount");
        }
        new File(filename).delete();
    }
    
    private static void testSkipsInvalidAmounts() throws IOException {
        String filename = "test_invalid.csv";
        Files.write(Paths.get(filename), "id,amount\n1,100.50\n2,invalid\n3,75.25\n".getBytes());
        
        double total = new SalesCalculator().calculateTotal(filename);
        assert Math.abs(total - 175.75) < 0.01 : "Expected 175.75 (skipped invalid), got " + total;
        new File(filename).delete();
    }
}
```

Copy these three classes into files (`SalesCalculator.java`, `SalesReader.java`, `SalesCalculatorTest.java`), compile, and run `SalesReader` to print the sum. Run `SalesCalculatorTest` to verify correctness.