import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SalesAnalyzer {
    public static void main(String[] args) {
        String filePath = "sales.csv";
        double totalAmount = calculateSalesSum(filePath);
        System.out.println("Total Sales Amount: " + totalAmount);
    }

    private static double calculateSalesSum(String filePath) {
        double sum = 0.0;

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                System.err.println("File is empty");
                return 0.0;
            }

            int amountColumnIndex = findColumnIndex(headerLine, "amount");
            if (amountColumnIndex == -1) {
                System.err.println("Column 'amount' not found");
                return 0.0;
            }

            String line;
            while ((line = reader.readLine()) != null) {
                sum += extractAmount(line, amountColumnIndex);
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }

        return sum;
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

    private static double extractAmount(String line, int columnIndex) {
        String[] values = line.split(",");
        if (columnIndex >= values.length) {
            return 0.0;
        }

        try {
            return Double.parseDouble(values[columnIndex].trim());
        } catch (NumberFormatException e) {
            System.err.println("Invalid amount value: " + values[columnIndex]);
            return 0.0;
        }
    }
}
