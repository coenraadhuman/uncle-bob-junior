import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SalesSumCalculator {
    private static final String CSV_FILE = "sales.csv";
    private static final String DELIMITER = ",";
    private static final String AMOUNT_COLUMN = "amount";

    public static void main(String[] args) {
        double totalAmount = calculateSalesSum();
        System.out.println("Total amount: " + totalAmount);
    }

    private static double calculateSalesSum() {
        try (BufferedReader reader = new BufferedReader(new FileReader(CSV_FILE))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                System.err.println("Error: CSV file is empty");
                return 0;
            }

            int amountColumnIndex = findColumnIndex(headerLine);
            if (amountColumnIndex == -1) {
                System.err.println("Error: 'amount' column not found");
                return 0;
            }

            return sumAmountColumn(reader, amountColumnIndex);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            return 0;
        }
    }

    private static int findColumnIndex(String headerLine) {
        String[] headers = headerLine.split(DELIMITER);
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].trim().equalsIgnoreCase(AMOUNT_COLUMN)) {
                return i;
            }
        }
        return -1;
    }

    private static double sumAmountColumn(BufferedReader reader, int columnIndex) throws IOException {
        double sum = 0;
        String line;
        int lineNumber = 2;

        while ((line = reader.readLine()) != null) {
            String[] values = line.split(DELIMITER);
            if (values.length > columnIndex) {
                try {
                    double amount = Double.parseDouble(values[columnIndex].trim());
                    sum += amount;
                } catch (NumberFormatException e) {
                    System.err.println("Warning: Invalid number at line " + lineNumber + ": " + values[columnIndex]);
                }
            }
            lineNumber++;
        }

        return sum;
    }
}
