import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SalesCalculator {
    public static void main(String[] args) {
        String filePath = "sales.csv";
        double totalAmount = 0;
        int amountColumnIndex = -1;

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isHeaderLine = true;

            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(",");

                if (isHeaderLine) {
                    for (int i = 0; i < columns.length; i++) {
                        if (columns[i].trim().equalsIgnoreCase("amount")) {
                            amountColumnIndex = i;
                            break;
                        }
                    }
                    isHeaderLine = false;

                    if (amountColumnIndex == -1) {
                        System.err.println("'amount' column not found in CSV");
                        return;
                    }
                } else {
                    if (amountColumnIndex < columns.length) {
                        try {
                            totalAmount += Double.parseDouble(columns[amountColumnIndex].trim());
                        } catch (NumberFormatException e) {
                            System.err.println("Skipping invalid amount value: " + columns[amountColumnIndex]);
                        }
                    }
                }
            }

            System.out.println("Total Amount: " + totalAmount);

        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
}
