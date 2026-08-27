import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class SalesSum {

    public static void main(String[] args) {
        String csvFile = "sales.csv";
        BigDecimal total = BigDecimal.ZERO;

        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
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
                System.out.println("No 'amount' column found in sales.csv.");
                return;
            }

            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }

                String[] fields = line.split(",");
                if (amountIndex >= fields.length) {
                    System.err.println("Skipping line " + lineNumber + ": missing amount field.");
                    continue;
                }

                String rawValue = fields[amountIndex].trim();
                try {
                    total = total.add(new BigDecimal(rawValue));
                } catch (NumberFormatException e) {
                    System.err.println("Skipping line " + lineNumber + ": invalid amount value '" + rawValue + "'.");
                }
            }

            System.out.println("Sum of 'amount' column: " + total.setScale(2, RoundingMode.HALF_UP));

        } catch (IOException e) {
            System.err.println("Error reading sales.csv: " + e.getMessage());
        }
    }
}
