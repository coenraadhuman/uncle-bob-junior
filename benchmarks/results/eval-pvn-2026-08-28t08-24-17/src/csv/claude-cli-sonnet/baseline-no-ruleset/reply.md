Assumptions: `sales.csv` is in the working directory, comma-delimited with a header row, and the `amount` column contains plain numeric values (no currency symbols/quoting).

```java
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class SalesSum {

    public static void main(String[] args) throws IOException {
        String filePath = "sales.csv";
        double total = 0.0;

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                System.out.println("File is empty.");
                return;
            }

            List<String> headers = Arrays.asList(headerLine.split(","));
            int amountIndex = headers.indexOf("amount");
            if (amountIndex == -1) {
                System.out.println("No 'amount' column found in header.");
                return;
            }

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                String[] fields = line.split(",", -1);
                if (amountIndex >= fields.length) {
                    continue;
                }
                String value = fields[amountIndex].trim();
                if (value.isEmpty()) {
                    continue;
                }
                try {
                    total += Double.parseDouble(value);
                } catch (NumberFormatException e) {
                    System.out.println("Skipping invalid amount value: " + value);
                }
            }
        }

        System.out.printf("Sum of amount column: %.2f%n", total);
    }
}
```

Run with `javac SalesSum.java && java SalesSum` from the directory containing `sales.csv`.