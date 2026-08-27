import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SalesCalculator {
    public static void main(String[] args) {
        try {
            double total = calculateSalesTotal("sales.csv");
            System.out.println("Total Amount: " + total);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }

    private static double calculateSalesTotal(String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return 0;
            }

            int amountColumnIndex = findColumnIndex(headerLine, "amount");
            if (amountColumnIndex == -1) {
                throw new IOException("Column 'amount' not found in CSV");
            }

            double total = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                total += parseAmount(line, amountColumnIndex);
            }

            return total;
        }
    }

    private static int findColumnIndex(String headerLine, String columnName) {
        String[] headers = headerLine.split(",");
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].trim().equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        return -1;
    }

    private static double parseAmount(String line, int columnIndex) {
        String[] fields = line.split(",");
        if (columnIndex >= fields.length) {
            return 0;
        }
        try {
            return Double.parseDouble(fields[columnIndex].trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
