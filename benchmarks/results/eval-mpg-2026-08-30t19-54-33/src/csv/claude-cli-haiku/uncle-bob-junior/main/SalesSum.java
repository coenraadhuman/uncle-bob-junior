import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SalesSum {
    public static void main(String[] args) throws IOException {
        double total = sumAmountColumn("sales.csv");
        System.out.println(total);
    }

    static double sumAmountColumn(String filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return 0.0;
            }
            
            int amountIndex = findColumnIndex(headerLine, "amount");
            if (amountIndex == -1) {
                throw new IllegalArgumentException("'amount' column not found");
            }
            
            double sum = 0.0;
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    sum += extractAmount(line, amountIndex);
                }
            }
            return sum;
        }
    }

    static int findColumnIndex(String header, String columnName) {
        String[] columns = header.split(",");
        for (int i = 0; i < columns.length; i++) {
            if (columns[i].trim().equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        return -1;
    }

    static double extractAmount(String line, int columnIndex) {
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
