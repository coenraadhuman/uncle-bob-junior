import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SalesAnalyzer {
    private static final String CSV_FILE = "sales.csv";
    private static final String AMOUNT_COLUMN = "amount";

    public static void main(String[] args) {
        try {
            System.out.println(calculateTotal());
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    static double calculateTotal() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(CSV_FILE))) {
            String header = reader.readLine();
            if (header == null) return 0.0;
            
            int columnIndex = findColumnIndex(header);
            if (columnIndex == -1) {
                throw new IllegalArgumentException("Column '" + AMOUNT_COLUMN + "' not found");
            }
            
            return sumAmounts(reader, columnIndex);
        }
    }

    static int findColumnIndex(String header) {
        String[] columns = header.split(",");
        for (int i = 0; i < columns.length; i++) {
            if (columns[i].trim().equalsIgnoreCase(AMOUNT_COLUMN)) {
                return i;
            }
        }
        return -1;
    }

    static double sumAmounts(BufferedReader reader, int columnIndex) throws IOException {
        double total = 0.0;
        String line;
        while ((line = reader.readLine()) != null) {
            if (!line.trim().isEmpty()) {
                total += extractAmount(line, columnIndex);
            }
        }
        return total;
    }

    static double extractAmount(String line, int columnIndex) {
        String[] values = line.split(",");
        if (columnIndex >= values.length) return 0.0;
        try {
            return Double.parseDouble(values[columnIndex].trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
