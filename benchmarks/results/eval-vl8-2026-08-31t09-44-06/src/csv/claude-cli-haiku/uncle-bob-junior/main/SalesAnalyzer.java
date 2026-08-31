import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SalesAnalyzer {
    private static final String AMOUNT_COLUMN = "amount";
    private static final String CSV_DELIMITER = ",";
    
    public static void main(String[] args) {
        try {
            double total = calculateSumFromCsv("sales.csv");
            System.out.println(total);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
    
    static double calculateSumFromCsv(String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String header = reader.readLine();
            if (header == null) {
                return 0;
            }
            
            int amountColumnIndex = findColumnIndex(header);
            if (amountColumnIndex < 0) {
                throw new IllegalArgumentException("Column 'amount' not found in CSV");
            }
            
            return sumColumn(reader, amountColumnIndex);
        }
    }
    
    private static int findColumnIndex(String header) {
        String[] columns = header.split(CSV_DELIMITER);
        for (int i = 0; i < columns.length; i++) {
            if (columns[i].trim().equalsIgnoreCase(AMOUNT_COLUMN)) {
                return i;
            }
        }
        return -1;
    }
    
    private static double sumColumn(BufferedReader reader, int columnIndex) throws IOException {
        double sum = 0;
        String line;
        while ((line = reader.readLine()) != null) {
            if (!line.trim().isEmpty()) {
                sum += parseAmount(line, columnIndex);
            }
        }
        return sum;
    }
    
    private static double parseAmount(String line, int columnIndex) {
        String[] values = line.split(CSV_DELIMITER);
        if (columnIndex >= values.length) {
            return 0;
        }
        try {
            return Double.parseDouble(values[columnIndex].trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
