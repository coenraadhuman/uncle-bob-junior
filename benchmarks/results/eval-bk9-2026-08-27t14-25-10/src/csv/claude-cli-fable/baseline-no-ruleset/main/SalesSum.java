import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;

public class SalesSum {

    public static void main(String[] args) {
        Path file = Path.of(args.length > 0 ? args[0] : "sales.csv");

        try (BufferedReader reader = Files.newBufferedReader(file)) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                System.err.println("Error: " + file + " is empty.");
                System.exit(1);
            }

            int amountIndex = findAmountColumn(headerLine.split(",", -1));
            if (amountIndex < 0) {
                System.err.println("Error: no 'amount' column found in header: " + headerLine);
                System.exit(1);
            }

            BigDecimal sum = BigDecimal.ZERO;
            String line;
            int lineNumber = 1;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }
                String[] fields = line.split(",", -1);
                if (fields.length <= amountIndex || fields[amountIndex].isBlank()) {
                    System.err.println("Warning: skipping line " + lineNumber + " (missing amount): " + line);
                    continue;
                }
                try {
                    sum = sum.add(new BigDecimal(fields[amountIndex].trim()));
                } catch (NumberFormatException e) {
                    System.err.println("Warning: skipping line " + lineNumber + " (invalid amount): " + line);
                }
            }

            System.out.println("Total amount: " + sum.toPlainString());
        } catch (IOException e) {
            System.err.println("Error reading " + file + ": " + e.getMessage());
            System.exit(1);
        }
    }

    private static int findAmountColumn(String[] headers) {
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].trim().equalsIgnoreCase("amount")) {
                return i;
            }
        }
        return -1;
    }
}
