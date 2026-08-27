import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SalesSumCalculator {
    public static void main(String[] args) {
        String filePath = "sales.csv";
        double total = calculateSum(filePath);
        System.out.println("Total amount: " + total);
    }

    private static double calculateSum(String filePath) {
        double sum = 0.0;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                System.err.println("CSV file is empty");
                return 0.0;
            }
            
            int amountColumnIndex = findColumnIndex(headerLine, "amount");
            if (amountColumnIndex == -1) {
                System.err.println("'amount' column not found");
                return 0.0;
            }
            
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                if (amountColumnIndex < fields.length) {
                    try {
                        sum += Double.parseDouble(fields[amountColumnIndex].trim());
                    } catch (NumberFormatException e) {
                        System.err.println("Skipping invalid amount value: " + fields[amountColumnIndex]);
                    }
                }
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
}
