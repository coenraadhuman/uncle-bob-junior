import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SalesAnalyzer {
    private static final String DEFAULT_FILE_PATH = "sales.csv";
    private static final String AMOUNT_COLUMN_NAME = "amount";

    public static void main(String[] args) {
        String filePath = args.length > 0 ? args[0] : DEFAULT_FILE_PATH;
        double total = sumAmountColumn(filePath);
        System.out.println("Total amount: " + total);
    }

    static double sumAmountColumn(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IllegalArgumentException("CSV file is empty");
            }

            int amountColumnIndex = findColumnIndex(headerLine, AMOUNT_COLUMN_NAME);
            if (amountColumnIndex == -1) {
                throw new IllegalArgumentException("Column '" + AMOUNT_COLUMN_NAME + "' not found");
            }

            return sumColumn(reader, amountColumnIndex);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: " + filePath, e);
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

    private static double sumColumn(BufferedReader reader, int columnIndex) throws IOException {
        double sum = 0.0;
        String line;
        while ((line = reader.readLine()) != null) {
            double value = parseAmount(line, columnIndex);
            sum += value;
        }
        return sum;
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
