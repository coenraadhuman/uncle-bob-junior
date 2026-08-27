import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class SalesSum {

    public static void main(String[] args) {
        String path = args.length > 0 ? args[0] : "sales.csv";
        double total = 0.0;

        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                System.out.println("File is empty.");
                return;
            }

            List<String> headers = List.of(headerLine.split(","));
            int amountIndex = headers.indexOf("amount");
            if (amountIndex == -1) {
                System.out.println("No 'amount' column found in header.");
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
                    System.out.println("Skipping line " + lineNumber + ": missing amount field.");
                    continue;
                }
                try {
                    total += Double.parseDouble(fields[amountIndex].trim());
                } catch (NumberFormatException e) {
                    System.out.println("Skipping line " + lineNumber + ": invalid amount value '" + fields[amountIndex] + "'.");
                }
            }

            System.out.printf("Sum of amount column: %.2f%n", total);

        } catch (IOException e) {
            System.out.println("Error reading file '" + path + "': " + e.getMessage());
        }
    }
}
