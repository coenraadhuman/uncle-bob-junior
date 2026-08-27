import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SalesSum {

    public static void main(String[] args) {
        String filePath = args.length > 0 ? args[0] : "sales.csv";
        double total = 0.0;
        int rowsProcessed = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
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
                System.err.println("Column 'amount' not found in header: " + headerLine);
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
                    System.err.println("Skipping line " + lineNumber + " (missing amount field): " + line);
                    continue;
                }

                String rawValue = fields[amountIndex].trim();
                try {
                    total += Double.parseDouble(rawValue);
                    rowsProcessed++;
                } catch (NumberFormatException e) {
                    System.err.println("Skipping line " + lineNumber + " (invalid amount '" + rawValue + "')");
                }
            }

        } catch (IOException e) {
            System.err.println("Error reading file '" + filePath + "': " + e.getMessage());
            return;
        }

        System.out.printf("Rows processed: %d%n", rowsProcessed);
        System.out.printf("Sum of amount: %.2f%n", total);
    }
}
