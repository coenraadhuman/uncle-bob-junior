import java.io.*;

public class SalesAnalyzer {
    private static final String AMOUNT_COLUMN = "amount";
    private static final int NOT_FOUND = -1;

    public static void main(String[] args) {
        String filePath = "sales.csv";
        try {
            double totalAmount = calculateTotalAmount(filePath);
            System.out.println("Total amount: " + totalAmount);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public static double calculateTotalAmount(String filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return 0.0;
            }

            int amountColumnIndex = findColumnIndex(headerLine, AMOUNT_COLUMN);
            if (amountColumnIndex == NOT_FOUND) {
                throw new IllegalArgumentException("Column '" + AMOUNT_COLUMN + "' not found");
            }

            double total = 0.0;
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    total += parseAmount(line, amountColumnIndex);
                }
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
        return NOT_FOUND;
    }

    private static double parseAmount(String line, int columnIndex) {
        String[] values = line.split(",");
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
