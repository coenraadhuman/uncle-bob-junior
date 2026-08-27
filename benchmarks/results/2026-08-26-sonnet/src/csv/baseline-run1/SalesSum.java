import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class SalesSum {

    public static void main(String[] args) {
        String fileName = args.length > 0 ? args[0] : "sales.csv";
        double total = 0.0;
        int amountIndex = -1;

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line = reader.readLine();
            if (line == null) {
                System.out.println("File is empty.");
                return;
            }

            List<String> header = Arrays.asList(line.split(",", -1));
            for (int i = 0; i < header.size(); i++) {
                if (header.get(i).trim().equalsIgnoreCase("amount")) {
                    amountIndex = i;
                    break;
                }
            }

            if (amountIndex == -1) {
                System.out.println("Column 'amount' not found in header.");
                return;
            }

            String row;
            int lineNumber = 1;
            while ((row = reader.readLine()) != null) {
                lineNumber++;
                if (row.isBlank()) {
                    continue;
                }
                String[] fields = row.split(",", -1);
                if (amountIndex >= fields.length) {
                    System.err.println("Skipping line " + lineNumber + ": missing amount field.");
                    continue;
                }
                String rawValue = fields[amountIndex].trim();
                try {
                    total += Double.parseDouble(rawValue);
                } catch (NumberFormatException e) {
                    System.err.println("Skipping line " + lineNumber + ": invalid amount '" + rawValue + "'.");
                }
            }

            System.out.printf("Sum of amount: %.2f%n", total);

        } catch (IOException e) {
            System.err.println("Error reading file '" + fileName + "': " + e.getMessage());
        }
    }
}
