import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SalesSum {

    private static final String COLUMN_NAME = "amount";
    private static final String CSV_DELIMITER = ",";
    private static final String CSV_FILE_PATH = "sales.csv";

    public static void main(String[] args) throws IOException {
        double total = sumAmountColumn(CSV_FILE_PATH);
        System.out.printf("Sum of '%s': %.2f%n", COLUMN_NAME, total);
    }

    private static double sumAmountColumn(String csvFilePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFilePath))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IOException("CSV file is empty: " + csvFilePath);
            }

            int amountIndex = findColumnIndex(headerLine.split(CSV_DELIMITER, -1));
            return sumRows(reader, amountIndex);
        }
    }

    private static int findColumnIndex(String[] headers) throws IOException {
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].trim().equalsIgnoreCase(COLUMN_NAME)) {
                return i;
            }
        }
        throw new IOException("Column '" + COLUMN_NAME + "' not found in header");
    }

    private static double sumRows(BufferedReader reader, int amountIndex) throws IOException {
        double total = 0.0;
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.isBlank()) {
                continue;
            }
            String[] fields = line.split(CSV_DELIMITER, -1);
            total += Double.parseDouble(fields[amountIndex].trim());
        }
        return total;
    }
}
