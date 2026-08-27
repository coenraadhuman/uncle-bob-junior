import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SalesSum {

    public static void main(String[] args) {
        String csvFile = "sales.csv";
        double total = 0.0;
        int amountIndex = -1;

        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
            String line = reader.readLine();
            if (line == null) {
                System.out.println("sales.csv is empty.");
                return;
            }

            String[] headers = line.split(",");
            for (int i = 0; i < headers.length; i++) {
                if (headers[i].trim().equalsIgnoreCase("amount")) {
                    amountIndex = i;
                    break;
                }
            }

            if (amountIndex == -1) {
                System.out.println("Could not find an 'amount' column in sales.csv.");
                return;
            }

            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }

                String[] fields = line.split(",");
                if (amountIndex >= fields.length) {
                    System.out.println("Skipping line " + lineNumber + ": missing amount value.");
                    continue;
                }

                String rawValue = fields[amountIndex].trim();
                try {
                    total += Double.parseDouble(rawValue);
                } catch (NumberFormatException e) {
                    System.out.println("Skipping line " + lineNumber + ": invalid amount '" + rawValue + "'.");
                }
            }

            System.out.printf("Sum of 'amount': %.2f%n", total);

        } catch (IOException e) {
            System.out.println("Error reading sales.csv: " + e.getMessage());
        }
    }
}
