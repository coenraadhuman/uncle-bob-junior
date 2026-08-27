import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class SalesCalculator {
    static class CsvRow {
        String[] headers;
        String[] values;

        CsvRow(String[] headers, String[] values) {
            this.headers = headers;
            this.values = values;
        }

        String getValue(String columnName) {
            for (int i = 0; i < headers.length; i++) {
                if (headers[i].equalsIgnoreCase(columnName)) {
                    return i < values.length ? values[i].trim() : "";
                }
            }
            throw new IllegalArgumentException("Column not found: " + columnName);
        }
    }

    static CsvRow parseRow(String[] headers, String line) {
        String[] values = line.split(",");
        return new CsvRow(headers, values);
    }

    static double sumAmountColumn(String filePath) throws IOException {
        double sum = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IOException("File is empty");
            }

            String[] headers = headerLine.split(",");
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                CsvRow row = parseRow(headers, line);
                String amountStr = row.getValue("amount");
                sum += Double.parseDouble(amountStr);
            }
        }
        return sum;
    }

    public static void main(String[] args) {
        try {
            double total = sumAmountColumn("sales.csv");
            System.out.println("Total amount: " + total);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            System.exit(1);
        } catch (NumberFormatException e) {
            System.err.println("Invalid number in amount column: " + e.getMessage());
            System.exit(1);
        }
    }
}
