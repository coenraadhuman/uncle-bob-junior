import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SalesCalculator {
    private static final String FILENAME = "sales.csv";
    private static final String AMOUNT_COLUMN = "amount";

    public static void main(String[] args) {
        try {
            double total = calculateTotalAmount();
            System.out.println("Total amount: " + total);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }

    private static double calculateTotalAmount() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILENAME))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IOException("CSV file is empty");
            }

            int amountColumnIndex = findColumnIndex(headerLine);
            if (amountColumnIndex == -1) {
                throw new IOException("Column '" + AMOUNT_COLUMN + "' not found in CSV");
            }

            return sumAmountColumn(reader, amountColumnIndex);
        }
    }

    private static int findColumnIndex(String headerLine) {
        String[] headers = headerLine.split(",");
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].trim().equalsIgnoreCase(AMOUNT_COLUMN)) {
                return i;
            }
        }
        return -1;
    }

    private static double sumAmountColumn(BufferedReader reader, int columnIndex) throws IOException {
        double total = 0.0;
        String line;

        while ((line = reader.readLine()) != null) {
            String[] fields = line.split(",");
            if (columnIndex < fields.length) {
                try {
                    total += Double.parseDouble(fields[columnIndex].trim());
                } catch (NumberFormatException e) {
                    System.err.println("Skipping non-numeric value: " + fields[columnIndex]);
                }
            }
        }

        return total;
    }
}
