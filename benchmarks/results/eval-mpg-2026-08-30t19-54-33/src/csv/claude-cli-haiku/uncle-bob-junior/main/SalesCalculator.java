import java.io.*;
import java.nio.file.*;

public class SalesCalculator {
    private static final String AMOUNT_COLUMN = "amount";

    public static void main(String[] args) throws IOException {
        double total = sumAmountsFromFile(Paths.get("sales.csv"));
        System.out.println(total);
    }

    public static double sumAmountsFromFile(Path filePath) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IllegalArgumentException("CSV file is empty");
            }

            int amountIndex = findColumnIndex(headerLine, AMOUNT_COLUMN);
            if (amountIndex == -1) {
                throw new IllegalArgumentException("Column '" + AMOUNT_COLUMN + "' not found");
            }

            return sumColumn(reader, amountIndex);
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
            String[] fields = line.split(",");
            if (fields.length > columnIndex) {
                String value = fields[columnIndex].trim();
                if (!value.isEmpty()) {
                    sum += Double.parseDouble(value);
                }
            }
        }
        return sum;
    }
}
