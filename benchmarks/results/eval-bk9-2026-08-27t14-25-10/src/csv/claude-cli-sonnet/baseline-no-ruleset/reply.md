Assumptions: `sales.csv` is comma-delimited with a header row containing a column named `amount`, values parse as `double`, and the file sits in the working directory (path can be passed as `args[0]`, defaulting to `sales.csv`). Fields are not quoted/escaped (simple CSV).

```java
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class SalesSum {

    public static void main(String[] args) {
        String path = args.length > 0 ? args[0] : "sales.csv";

        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
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

            double sum = 0.0;
            String line;
            int lineNumber = 1;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }

                String[] fields = line.split(",");
                if (amountIndex >= fields.length) {
                    System.out.println("Skipping malformed line " + lineNumber + ": " + line);
                    continue;
                }

                try {
                    sum += Double.parseDouble(fields[amountIndex].trim());
                } catch (NumberFormatException e) {
                    System.out.println("Skipping non-numeric amount on line " + lineNumber + ": " + fields[amountIndex]);
                }
            }

            System.out.printf("Sum of 'amount': %.2f%n", sum);

        } catch (IOException e) {
            System.err.println("Error reading file '" + path + "': " + e.getMessage());
        }
    }
}
```

Usage: `java SalesSum.java` (reads `sales.csv` in the current directory) or `java SalesSum.java /path/to/sales.csv`.