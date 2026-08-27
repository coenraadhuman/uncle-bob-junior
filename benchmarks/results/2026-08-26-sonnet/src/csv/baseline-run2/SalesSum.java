import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SalesSum {

    public static void main(String[] args) {
        String csvFile = "sales.csv";
        double total = 0.0;
        int amountIndex = -1;
        int lineNumber = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }
                String[] fields = line.split(",");

                if (lineNumber == 1) {
                    for (int i = 0; i < fields.length; i++) {
                        if (fields[i].trim().equalsIgnoreCase("amount")) {
                            amountIndex = i;
                            break;
                        }
                    }
                    if (amountIndex == -1) {
                        System.err.println("Could not find an 'amount' column in the header.");
                        return;
                    }
                    continue;
                }

                if (amountIndex >= fields.length) {
                    System.err.println("Skipping line " + lineNumber + ": missing amount field.");
                    continue;
                }

                String rawValue = fields[amountIndex].trim();
                if (rawValue.isEmpty()) {
                    continue;
                }

                try {
                    total += Double.parseDouble(rawValue);
                } catch (NumberFormatException e) {
                    System.err.println("Skipping line " + lineNumber + ": invalid amount '" + rawValue + "'.");
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading " + csvFile + ": " + e.getMessage());
            return;
        }

        System.out.printf("Sum of 'amount': %.2f%n", total);
    }
}
