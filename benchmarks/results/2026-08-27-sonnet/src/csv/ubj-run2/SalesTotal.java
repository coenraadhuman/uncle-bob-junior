import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SalesTotal {

    private static final String CSV_FILE_PATH = "sales.csv";
    private static final String COLUMN_DELIMITER = ",";
    private static final String AMOUNT_COLUMN_NAME = "amount";

    public static void main(String[] args) throws IOException {
        double totalAmount = sumAmountColumn(CSV_FILE_PATH);
        System.out.printf("Total amount: %.2f%n", totalAmount);
    }

    private static double sumAmountColumn(String filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return 0.0;
            }
            int amountColumnIndex = findAmountColumnIndex(headerLine);

            double total = 0.0;
            String row;
            while ((row = reader.readLine()) != null) {
                if (row.isBlank()) {
                    continue;
                }
                total += parseAmount(row, amountColumnIndex);
            }
            return total;
        }
    }

    private static int findAmountColumnIndex(String headerLine) {
        String[] columnNames = headerLine.split(COLUMN_DELIMITER);
        for (int i = 0; i < columnNames.length; i++) {
            if (columnNames[i].trim().equalsIgnoreCase(AMOUNT_COLUMN_NAME)) {
                return i;
            }
        }
        throw new IllegalStateException("CSV header has no '" + AMOUNT_COLUMN_NAME + "' column");
    }

    private static double parseAmount(String row, int amountColumnIndex) {
        String[] fields = row.split(COLUMN_DELIMITER);
        return Double.parseDouble(fields[amountColumnIndex].trim());
    }
}
