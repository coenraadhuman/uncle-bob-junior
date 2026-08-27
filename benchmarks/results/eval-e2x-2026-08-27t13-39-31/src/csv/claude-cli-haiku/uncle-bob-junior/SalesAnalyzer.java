import java.io.*;

public class SalesAnalyzer {
    private static final String CSV_FILE = "sales.csv";
    private static final String AMOUNT_COLUMN = "amount";
    
    public static void main(String[] args) {
        try {
            double totalAmount = calculateTotalAmount();
            System.out.println("Total Amount: " + totalAmount);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
    
    private static double calculateTotalAmount() throws IOException {
        double total = 0;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(CSV_FILE))) {
            String header = reader.readLine();
            if (header == null) {
                throw new IOException("CSV file is empty");
            }
            
            int amountColumnIndex = findColumnIndex(header, AMOUNT_COLUMN);
            if (amountColumnIndex == -1) {
                throw new IOException("'" + AMOUNT_COLUMN + "' column not found");
            }
            
            String line;
            while ((line = reader.readLine()) != null) {
                total += extractAmount(line, amountColumnIndex);
            }
        }
        
        return total;
    }
    
    private static int findColumnIndex(String header, String columnName) {
        String[] columns = header.split(",");
        for (int i = 0; i < columns.length; i++) {
            if (columns[i].trim().equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        return -1;
    }
    
    private static double extractAmount(String line, int columnIndex) {
        String[] values = line.split(",");
        if (columnIndex >= values.length) {
            return 0;
        }
        
        try {
            return Double.parseDouble(values[columnIndex].trim());
        } catch (NumberFormatException e) {
            System.err.println("Invalid amount value: '" + values[columnIndex] + "'");
            return 0;
        }
    }
}
