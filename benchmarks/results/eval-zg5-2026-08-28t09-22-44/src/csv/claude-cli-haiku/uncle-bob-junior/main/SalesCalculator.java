import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SalesCalculator {
    private static final String AMOUNT_COLUMN = "amount";
    
    public double calculateTotal(String filename) throws IOException {
        double sum = 0.0;
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String header = reader.readLine();
            if (header == null) {
                return 0.0;
            }
            
            int amountIndex = findColumnIndex(header, AMOUNT_COLUMN);
            if (amountIndex == -1) {
                throw new IllegalArgumentException("Column '" + AMOUNT_COLUMN + "' not found");
            }
            
            String line;
            while ((line = reader.readLine()) != null) {
                sum += extractAmount(line, amountIndex);
            }
        }
        return sum;
    }
    
    private int findColumnIndex(String header, String columnName) {
        String[] columns = header.split(",");
        for (int i = 0; i < columns.length; i++) {
            if (columns[i].trim().equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        return -1;
    }
    
    private double extractAmount(String line, int columnIndex) {
        String[] fields = line.split(",");
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
