import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SalesSum {

    public static void main(String[] args) {
        String fileName = args.length > 0 ? args[0] : "sales.csv";
        double total = 0.0;
        int amountColumnIndex = -1;
        int rowCount = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line = reader.readLine();
            if (line == null) {
                System.out.println("File is empty.");
                return;
            }

            String[] headers = line.split(",", -1);
            for (int i = 0; i < headers.length; i++) {
                if (headers[i].trim().equalsIgnoreCase("amount")) {
                    amountColumnIndex = i;
                    break;
                }
            }

            if (amountColumnIndex == -1) {
                System.out.println("Could not find an 'amount' column in the header.");
                return;
            }

            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                String[] fields = line.split(",", -1);
                if (amountColumnIndex >= fields.length) {
                    continue;
                }
                String value = fields[amountColumnIndex].trim();
                if (value.isEmpty()) {
                    continue;
                }
                try {
                    total += Double.parseDouble(value);
                    rowCount++;
                } catch (NumberFormatException e) {
                    System.err.println("Skipping unparseable amount value: " + value);
                }
            }

            System.out.printf("Sum of 'amount' over %d rows: %.2f%n", rowCount, total);

        } catch (IOException e) {
            System.err.println("Error reading file '" + fileName + "': " + e.getMessage());
        }
    }
}
