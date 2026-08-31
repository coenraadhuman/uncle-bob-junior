import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SalesCalculator {
    private static final String AMOUNT_HEADER = "amount";
    private static final String CSV_FILE = "sales.csv";
    
    public static void main(String[] args) {
        try {
            double total = calculateTotalSales(CSV_FILE);
            System.out.println("Total sales: " + total);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid CSV format: " + e.getMessage());
        }
    }
    
    public static double calculateTotalSales(String filename) throws IOException {
        var lines = Files.readAllLines(Paths.get(filename));
        if (lines.isEmpty()) {
            return 0.0;
        }
        
        var headers = lines.get(0).split(",");
        int amountColumnIndex = findColumnIndex(headers, AMOUNT_HEADER);
        if (amountColumnIndex < 0) {
            throw new IllegalArgumentException("Column 'amount' not found");
        }
        
        return lines.stream()
            .skip(1)
            .mapToDouble(line -> parseAmount(line.split(","), amountColumnIndex))
            .sum();
    }
    
    private static int findColumnIndex(String[] headers, String columnName) {
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].trim().equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        return -1;
    }
    
    private static double parseAmount(String[] fields, int columnIndex) {
        if (columnIndex >= fields.length) {
            return 0.0;
        }
        try {
            return Double.parseDouble(fields[columnIndex].trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
