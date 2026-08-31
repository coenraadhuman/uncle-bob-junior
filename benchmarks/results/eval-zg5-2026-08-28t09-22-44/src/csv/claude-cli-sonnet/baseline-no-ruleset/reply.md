Assumptions: `sales.csv` is in the working directory, comma-separated with a header row containing a column named `amount`, and fields aren't quoted/escaped (simple CSV).

```java
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Locale;

public class SalesSum {

    public static void main(String[] args) throws IOException {
        double total = 0.0;

        try (BufferedReader reader = new BufferedReader(new FileReader("sales.csv"))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                System.out.println("sales.csv is empty");
                return;
            }

            String[] headers = headerLine.split(",");
            int amountIndex = -1;
            for (int i = 0; i < headers.length; i++) {
                if (headers[i].trim().equalsIgnoreCase("amount")) {
                    amountIndex = i;
                    break;
                }
            }

            if (amountIndex == -1) {
                System.out.println("No 'amount' column found in sales.csv");
                return;
            }

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                String[] fields = line.split(",");
                if (amountIndex < fields.length) {
                    String value = fields[amountIndex].trim();
                    if (!value.isEmpty()) {
                        total += Double.parseDouble(value);
                    }
                }
            }
        }

        System.out.printf(Locale.US, "Sum of amount: %.2f%n", total);
    }
}
```

To run: `javac SalesSum.java && java SalesSum` with `sales.csv` in the same directory.