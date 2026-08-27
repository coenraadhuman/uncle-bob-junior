import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SalesSum {

    public static void main(String[] args) {
        String fileName = "sales.csv";
        double total = 0.0;
        int rowsProcessed = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                System.out.println("File is empty.");
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
                System.out.println("Column 'amount' not found in header.");
                return;
            }

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                String[] fields = line.split(",");
                if (amountIndex >= fields.length) {
                    continue;
                }
                String rawValue = fields[amountIndex].trim();
                if (rawValue.isEmpty()) {
                    continue;
                }
                try {
                    total += Double.parseDouble(rawValue);
                    rowsProcessed++;
                } catch (NumberFormatException e) {
                    System.out.println("Skipping unparsable value: " + rawValue);
                }
            }

            System.out.printf("Sum of 'amount' over %d rows: %.2f%n", rowsProcessed, total);

        } catch (IOException e) {
            System.out.println("Error reading file '" + fileName + "': " + e.getMessage());
        }
    }
}
