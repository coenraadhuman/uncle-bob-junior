import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class SalesTotal {

    private static final String DEFAULT_CSV_PATH = "sales.csv";
    private static final String AMOUNT_COLUMN = "amount";
    private static final String DELIMITER = ",";

    public static void main(String[] args) throws IOException {
        Path csvPath = Paths.get(args.length > 0 ? args[0] : DEFAULT_CSV_PATH);
        double total = sumColumn(csvPath, AMOUNT_COLUMN);
        System.out.printf("Total %s: %.2f%n", AMOUNT_COLUMN, total);
    }

    private static double sumColumn(Path csvPath, String columnName) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(csvPath)) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IllegalArgumentException("CSV file is empty: " + csvPath);
            }
            int columnIndex = findColumnIndex(headerLine, columnName);
            return sumRows(reader, columnIndex);
        }
    }

    private static int findColumnIndex(String headerLine, String columnName) {
        String[] headers = headerLine.split(DELIMITER);
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].trim().equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Column not found: " + columnName);
    }

    private static double sumRows(BufferedReader reader, int columnIndex) throws IOException {
        double total = 0.0;
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.isBlank()) {
                continue;
            }
            String[] fields = line.split(DELIMITER);
            total += Double.parseDouble(fields[columnIndex].trim());
        }
        return total;
    }
}
