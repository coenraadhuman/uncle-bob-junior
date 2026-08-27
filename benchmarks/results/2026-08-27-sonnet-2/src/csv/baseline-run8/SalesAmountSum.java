import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SalesAmountSum {

    public static void main(String[] args) {
        String filePath = "sales.csv";
        double total = 0.0;
        int rowsRead = 0;
        int rowsSkipped = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                System.out.println("sales.csv is empty.");
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
                System.out.println("Could not find an 'amount' column in the header.");
                return;
            }

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                String[] fields = line.split(",");
                if (amountIndex >= fields.length) {
                    rowsSkipped++;
                    continue;
                }
                try {
                    total += Double.parseDouble(fields[amountIndex].trim());
                    rowsRead++;
                } catch (NumberFormatException e) {
                    rowsSkipped++;
                }
            }

            System.out.printf("Sum of 'amount': %.2f%n", total);
            System.out.println("Rows summed: " + rowsRead + ", rows skipped: " + rowsSkipped);

        } catch (IOException e) {
            System.err.println("Error reading " + filePath + ": " + e.getMessage());
        }
    }
}
