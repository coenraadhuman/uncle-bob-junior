import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SalesTotal {

    private static final String CSV_DELIMITER = ",";
    private static final String AMOUNT_COLUMN_NAME = "amount";
    private static final String SALES_CSV_PATH = "sales.csv";

    private SalesTotal() {
    }

    public static void main(String[] args) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(Path.of(SALES_CSV_PATH))) {
            double total = sumAmountColumn(reader);
            System.out.println(total);
        }
    }

    static double sumAmountColumn(BufferedReader reader) throws IOException {
        String headerLine = reader.readLine();
        if (headerLine == null) {
            return 0.0;
        }

        int amountColumnIndex = findColumnIndex(headerLine, AMOUNT_COLUMN_NAME);
        double total = 0.0;
        String line;
        while ((line = reader.readLine()) != null) {
            total += parseAmount(line, amountColumnIndex);
        }
        return total;
    }

    private static int findColumnIndex(String headerLine, String columnName) {
        String[] columns = headerLine.split(CSV_DELIMITER);
        for (int i = 0; i < columns.length; i++) {
            if (columns[i].trim().equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Column '" + columnName + "' not found in header");
    }

    private static double parseAmount(String line, int amountColumnIndex) {
        String[] fields = line.split(CSV_DELIMITER);
        return Double.parseDouble(fields[amountColumnIndex].trim());
    }
}
