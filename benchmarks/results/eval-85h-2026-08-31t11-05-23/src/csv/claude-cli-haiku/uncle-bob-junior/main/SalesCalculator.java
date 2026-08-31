import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SalesCalculator {
    private static final String CSV_FILE = "sales.csv";
    private static final String DELIMITER = ",";

    public static void main(String[] args) {
        try {
            double total = sumSalesFromFile(CSV_FILE);
            System.out.println("Total sales amount: " + total);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid data: " + e.getMessage());
        }
    }

    static double sumSalesFromFile(String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            return sumSalesFromReader(reader);
        }
    }

    static double sumSalesFromReader(BufferedReader reader) throws IOException {
        String headerLine = reader.readLine();
        if (headerLine == null) {
            throw new IllegalArgumentException("CSV file is empty");
        }

        int amountIndex = findColumnIndex(headerLine, "amount");
        double sum = 0;
        String line;
        
        while ((line = reader.readLine()) != null) {
            if (!line.trim().isEmpty()) {
                sum += extractAmount(line, amountIndex);
            }
        }
        
        return sum;
    }

    private static int findColumnIndex(String header, String columnName) {
        String[] columns = header.split(DELIMITER);
        for (int i = 0; i < columns.length; i++) {
            if (columns[i].trim().equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Column '" + columnName + "' not found");
    }

    private static double extractAmount(String line, int columnIndex) {
        String[] fields = line.split(DELIMITER);
        if (columnIndex >= fields.length) {
            throw new IllegalArgumentException("Row missing expected column");
        }
        try {
            return Double.parseDouble(fields[columnIndex].trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                "Invalid amount: " + fields[columnIndex]);
        }
    }
}
