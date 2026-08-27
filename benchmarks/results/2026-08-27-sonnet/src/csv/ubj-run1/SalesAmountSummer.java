import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class SalesAmountSummer {

    private static final String CSV_FILE = "sales.csv";
    private static final String AMOUNT_COLUMN = "amount";
    private static final String CSV_DELIMITER = ",";

    public static void main(String[] args) throws IOException {
        double total = sumAmountColumn(CSV_FILE);
        System.out.printf("Sum of '%s': %.2f%n", AMOUNT_COLUMN, total);
    }

    private static double sumAmountColumn(String csvFile) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IOException("CSV file is empty: " + csvFile);
            }

            int amountIndex = findColumnIndex(headerLine, AMOUNT_COLUMN);
            return sumRows(reader, amountIndex);
        }
    }

    private static int findColumnIndex(String headerLine, String columnName) throws IOException {
        List<String> headers = Arrays.asList(headerLine.split(CSV_DELIMITER, -1));
        int index = headers.indexOf(columnName);
        if (index == -1) {
            throw new IOException("Column '" + columnName + "' not found in header: " + headerLine);
        }
        return index;
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
