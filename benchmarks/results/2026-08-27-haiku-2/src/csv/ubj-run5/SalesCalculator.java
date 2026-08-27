import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SalesCalculator {
    private static final String CSV_FILE = "sales.csv";
    private static final String AMOUNT_COLUMN = "amount";

    public static void main(String[] args) {
        try {
            double total = calculateSalesSum();
            System.out.println("Total sales: " + total);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }

    private static double calculateSalesSum() throws IOException {
        double sum = 0.0;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(CSV_FILE))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IOException("CSV file is empty");
            }
            
            int amountColumnIndex = findColumnIndex(headerLine, AMOUNT_COLUMN);
            if (amountColumnIndex == -1) {
                throw new IOException("Column '" + AMOUNT_COLUMN + "' not found");
            }
            
            String line;
            while ((line = reader.readLine()) != null) {
                sum += parseAmount(line, amountColumnIndex);
            }
        }
        
        return sum;
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
