import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class SalesSum {

    public static void main(String[] args) {
        String csvPath = args.length > 0 ? args[0] : "sales.csv";

        try (BufferedReader reader = new BufferedReader(new FileReader(csvPath))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                System.out.println("sales.csv is empty");
                return;
            }

            List<String> headers = List.of(headerLine.split(","));
            int amountIndex = headers.indexOf("amount");
            if (amountIndex == -1) {
                System.out.println("No 'amount' column found in header");
                return;
            }

            double sum = 0.0;
            String line;
            int lineNumber = 1;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }

                String[] fields = line.split(",", -1);
                if (amountIndex >= fields.length) {
                    System.out.println("Skipping malformed row " + lineNumber);
                    continue;
                }

                String rawValue = fields[amountIndex].trim();
                try {
                    sum += Double.parseDouble(rawValue);
                } catch (NumberFormatException e) {
                    System.out.println("Skipping non-numeric amount on row " + lineNumber + ": " + rawValue);
                }
            }

            System.out.printf("Sum of 'amount': %.2f%n", sum);

        } catch (IOException e) {
            System.err.println("Failed to read " + csvPath + ": " + e.getMessage());
        }
    }
}
