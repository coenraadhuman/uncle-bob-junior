import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public class SalesSum {

    public static void main(String[] args) {
        String filePath = args.length > 0 ? args[0] : "sales.csv";
        Path path = Path.of(filePath);

        double total = 0.0;
        int rowsProcessed = 0;
        int rowsSkipped = 0;

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                System.err.println("File is empty: " + filePath);
                return;
            }

            String[] headers = headerLine.split(",", -1);
            int amountIndex = -1;
            for (int i = 0; i < headers.length; i++) {
                if (headers[i].trim().equalsIgnoreCase("amount")) {
                    amountIndex = i;
                    break;
                }
            }

            if (amountIndex == -1) {
                System.err.println("No 'amount' column found in header: " + headerLine);
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
                    rowsSkipped++;
                    continue;
                }

                String rawValue = fields[amountIndex].trim();
                try {
                    total += Double.parseDouble(rawValue);
                    rowsProcessed++;
                } catch (NumberFormatException e) {
                    System.err.println("Skipping line " + lineNumber + ": invalid amount '" + rawValue + "'");
                    rowsSkipped++;
                }
            }

        } catch (IOException e) {
            System.err.println("Failed to read file '" + filePath + "': " + e.getMessage());
            return;
        }

        System.out.printf(Locale.US, "Sum of 'amount': %.2f%n", total);
        System.out.println("Rows processed: " + rowsProcessed + ", rows skipped: " + rowsSkipped);
    }
}
