import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SalesAmountSum {

    private static final String CSV_DELIMITER = ",";
    private static final String AMOUNT_COLUMN_NAME = "amount";

    public static void main(String[] args) throws IOException {
        String csvPath = args.length > 0 ? args[0] : "sales.csv";
        double total = sumAmountColumn(csvPath);
        System.out.printf("Total amount: %.2f%n", total);
    }

    private static double sumAmountColumn(String csvPath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(csvPath))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IOException("CSV file is empty: " + csvPath);
            }

            int amountColumnIndex = findAmountColumnIndex(headerLine);
            double total = 0.0;
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                total += parseAmount(line, amountColumnIndex);
            }
            return total;
        }
    }

    private static int findAmountColumnIndex(String headerLine) throws IOException {
        String[] headers = headerLine.split(CSV_DELIMITER);
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].trim().equalsIgnoreCase(AMOUNT_COLUMN_NAME)) {
                return i;
            }
        }
        throw new IOException("Column '" + AMOUNT_COLUMN_NAME + "' not found in CSV header");
    }

    private static double parseAmount(String line, int amountColumnIndex) throws IOException {
        String[] fields = line.split(CSV_DELIMITER);
        if (amountColumnIndex >= fields.length) {
            throw new IOException("Row is missing the amount field: " + line);
        }
        return Double.parseDouble(fields[amountColumnIndex].trim());
    }
}
