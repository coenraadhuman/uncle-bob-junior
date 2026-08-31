import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class SalesProcessor {
    public static void main(String[] args) throws IOException {
        double total = sumAmountsFromCsv("sales.csv");
        System.out.println("Total: " + total);
    }
    
    static double sumAmountsFromCsv(String filePath) throws IOException {
        List<String> lines = Files.readAllLines(Path.of(filePath));
        return sumAmounts(lines);
    }
    
    static double sumAmounts(List<String> lines) {
        if (lines.isEmpty()) {
            return 0.0;
        }
        
        int amountColumnIndex = findColumnIndex(lines.get(0), "amount");
        
        return lines.stream()
                    .skip(1)
                    .mapToDouble(line -> parseAmount(line, amountColumnIndex))
                    .sum();
    }
    
    private static int findColumnIndex(String headerLine, String columnName) {
        String[] headers = headerLine.split(",");
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].trim().equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Column not found: " + columnName);
    }
    
    private static double parseAmount(String line, int columnIndex) {
        String[] fields = line.split(",");
        return Double.parseDouble(fields[columnIndex].trim());
    }
}
