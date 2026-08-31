import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class SalesAnalyzer {
    private static final String AMOUNT_COLUMN = "amount";
    
    public static void main(String[] args) throws IOException {
        double sum = calculateSalesSum(Path.of("sales.csv"));
        System.out.println("Total: " + sum);
    }
    
    static double calculateSalesSum(Path csvFile) throws IOException {
        List<String> lines = Files.readAllLines(csvFile);
        if (lines.isEmpty()) {
            return 0.0;
        }
        
        int amountColumnIndex = findColumnIndex(lines.get(0));
        return sumAmountColumn(lines, amountColumnIndex);
    }
    
    private static int findColumnIndex(String headerLine) {
        String[] headers = headerLine.split(",");
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].trim().equalsIgnoreCase(AMOUNT_COLUMN)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Column '" + AMOUNT_COLUMN + "' not found");
    }
    
    private static double sumAmountColumn(List<String> lines, int columnIndex) {
        double sum = 0.0;
        for (int i = 1; i < lines.size(); i++) {
            String[] values = lines.get(i).split(",");
            if (columnIndex < values.length) {
                try {
                    sum += Double.parseDouble(values[columnIndex].trim());
                } catch (NumberFormatException e) {
                    // Skip rows with invalid amounts
                }
            }
        }
        return sum;
    }
}
