Assumptions: `sales.csv` is in the working directory, has a header row, is comma-separated (no quoted commas), and the `amount` column contains plain decimal numbers (using `.` as the decimal separator). I use `BigDecimal` to avoid floating-point rounding issues.

```java
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class SalesSum {

    public static void main(String[] args) {
        String fileName = args.length > 0 ? args[0] : "sales.csv";
        BigDecimal total = BigDecimal.ZERO;

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                System.out.println("File is empty.");
                return;
            }

            List<String> headers = Arrays.asList(headerLine.split(",", -1));
            int amountIndex = headers.indexOf("amount");
            if (amountIndex == -1) {
                System.out.println("No 'amount' column found.");
                return;
            }

            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }
                String[] fields = line.split(",", -1);
                if (amountIndex >= fields.length) {
                    System.err.println("Skipping malformed row " + lineNumber);
                    continue;
                }
                String value = fields[amountIndex].trim();
                if (value.isEmpty()) {
                    continue;
                }
                try {
                    total = total.add(new BigDecimal(value));
                } catch (NumberFormatException e) {
                    System.err.println("Skipping invalid amount on row " + lineNumber + ": " + value);
                }
            }

            System.out.println("Sum of amount: " + total);

        } catch (IOException e) {
            System.err.println("Error reading file '" + fileName + "': " + e.getMessage());
        }
    }
}
```

Compile and run with `javac SalesSum.java && java SalesSum` (pass a different path as an argument if the file isn't named `sales.csv` in the current directory).