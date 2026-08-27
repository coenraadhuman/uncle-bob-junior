import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SalesAnalyzer {
    private static final String AMOUNT_COLUMN = "amount";
    
    public static void main(String[] args) {
        String filePath = "sales.csv";
        double totalAmount = sumAmountColumn(filePath);
        System.out.println("Total amount: " + totalAmount);
    }
    
    private static double sumAmountColumn(String filePath) {
        double total = 0.0;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return 0.0;
            }
            
            int amountColumnIndex = findColumnIndex(headerLine, AMOUNT_COLUMN);
            if (amountColumnIndex < 0) {
                System.err.println("Column '" + AMOUNT_COLUMN + "' not found");
                return 0.0;
            }
            
            String line;
            while ((line = reader.readLine()) != null) {
                total += parseAmount(line, amountColumnIndex);
            }
            
            return total;
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            return 0.0;
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
    
    private static double parseAmount(String line, int columnIndex) {
        String[] values = line.split(",");
        if (columnIndex >= values.length) {
            return 0.0;
        }
        
        try {
            return Double.parseDouble(values[columnIndex].trim());
        } catch (NumberFormatException e) {
            System.err.println("Warning: Could not parse amount: " + values[columnIndex]);
            return 0.0;
        }
    }
}
