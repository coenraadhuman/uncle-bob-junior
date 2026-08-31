Here's a complete solution. Assumptions: `sales.csv` is in the working directory, has a header row containing an `amount` column (matched case-insensitively), fields are comma-separated without embedded commas in quoted values, and blank or non-numeric amount values are skipped with a warning.

```java
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class SalesSum {

    public static void main(String[] args) {
        Path file = Path.of(args.length > 0 ? args[0] : "sales.csv");

        List<String> lines;
        try {
            lines = Files.readAllLines(file);
        } catch (IOException e) {
            System.err.println("Could not read " + file + ": " + e.getMessage());
            System.exit(1);
            return;
        }

        if (lines.isEmpty()) {
            System.err.println("File is empty: " + file);
            System.exit(1);
            return;
        }

        String[] headers = lines.get(0).split(",", -1);
        int amountIndex = -1;
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].trim().equalsIgnoreCase("amount")) {
                amountIndex = i;
                break;
            }
        }
        if (amountIndex == -1) {
            System.err.println("No 'amount' column found in header: " + lines.get(0));
            System.exit(1);
            return;
        }

        // BigDecimal avoids floating-point drift when summing monetary values
        BigDecimal sum = BigDecimal.ZERO;
        for (int lineNo = 1; lineNo < lines.size(); lineNo++) {
            String line = lines.get(lineNo);
            if (line.isBlank()) {
                continue;
            }
            String[] fields = line.split(",", -1);
            if (fields.length <= amountIndex) {
                System.err.println("Skipping line " + (lineNo + 1) + ": too few columns");
                continue;
            }
            String value = fields[amountIndex].trim();
            if (value.isEmpty()) {
                continue;
            }
            try {
                sum = sum.add(new BigDecimal(value));
            } catch (NumberFormatException e) {
                System.err.println("Skipping line " + (lineNo + 1) + ": invalid amount '" + value + "'");
            }
        }

        System.out.println(sum.toPlainString());
    }
}
```

Compile and run with:

```
javac SalesSum.java
java SalesSum
```

You can also pass a different path as the first argument, for example `java SalesSum data/sales.csv`. It uses `BigDecimal` rather than `double` so sums of monetary amounts stay exact. If your CSV contains quoted fields with embedded commas, a proper CSV parser such as Apache Commons CSV would be the better choice; let me know if you need that version.