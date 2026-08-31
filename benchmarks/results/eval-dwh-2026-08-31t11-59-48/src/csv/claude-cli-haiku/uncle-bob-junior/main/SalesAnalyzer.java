import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SalesAnalyzer {
    private static final String CSV_FILE = "sales.csv";
    private static final String DELIMITER = ",";

    public static void main(String[] args) {
        try {
            double total = sumAmountColumn(CSV_FILE);
            System.out.println("Total amount: " + total);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    static double sumAmountColumn(String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return 0.0;
            }

            int amountColumnIndex = findAmountColumnIndex(headerLine);
            if (amountColumnIndex == -1) {
                throw new IllegalArgumentException("'amount' column not found in CSV header");
            }

            double sum = 0.0;
            String line;
            while ((line = reader.readLine()) != null) {
                sum += parseAmount(line, amountColumnIndex);
            }
            return sum;
        }
    }

    private static int findAmountColumnIndex(String headerLine) {
        String[] headers = headerLine.split(DELIMITER);
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].trim().equalsIgnoreCase("amount")) {
                return i;
            }
        }
        return -1;
    }

    private static double parseAmount(String line, int columnIndex) {
        String[] values = line.split(DELIMITER);
        if (columnIndex >= values.length) {
            return 0.0;
        }
        try {
            return Double.parseDouble(values[columnIndex].trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
