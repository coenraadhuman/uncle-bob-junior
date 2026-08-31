import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SalesAnalyzer {
    public static void main(String[] args) {
        double total = new SalesAnalyzer().sumSalesFromFile("sales.csv");
        System.out.println("Total amount: " + total);
    }
    
    double sumSalesFromFile(String filePath) {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filePath))) {
            return sumFromReader(reader);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read: " + filePath, e);
        }
    }
    
    double sumFromReader(BufferedReader reader) throws Exception {
        String header = reader.readLine();
        validateHeader(header);
        int amountIndex = findColumnIndex(header, "amount");
        
        double sum = 0;
        String line;
        while ((line = reader.readLine()) != null) {
            sum += extractAmount(line, amountIndex);
        }
        return sum;
    }
    
    private void validateHeader(String header) {
        if (header == null || header.isEmpty()) {
            throw new IllegalArgumentException("CSV file is empty or has no header");
        }
    }
    
    private int findColumnIndex(String header, String columnName) {
        String[] columns = header.split(",");
        for (int i = 0; i < columns.length; i++) {
            if (columns[i].trim().equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Column not found: " + columnName);
    }
    
    private double extractAmount(String line, int columnIndex) {
        String[] fields = line.split(",");
        if (columnIndex >= fields.length) {
            throw new IllegalArgumentException("Row missing amount column");
        }
        return Double.parseDouble(fields[columnIndex].trim());
    }
}
