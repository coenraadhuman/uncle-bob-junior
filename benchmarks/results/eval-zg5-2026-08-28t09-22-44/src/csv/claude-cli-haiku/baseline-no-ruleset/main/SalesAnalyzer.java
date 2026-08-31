import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SalesAnalyzer {
    public static void main(String[] args) {
        String filePath = "sales.csv";
        double sum = 0;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line = reader.readLine();
            if (line == null) {
                System.out.println("File is empty");
                return;
            }
            
            // Parse header to find the column index of "amount"
            String[] headers = line.split(",");
            int amountIndex = -1;
            for (int i = 0; i < headers.length; i++) {
                if (headers[i].trim().equalsIgnoreCase("amount")) {
                    amountIndex = i;
                    break;
                }
            }
            
            if (amountIndex == -1) {
                System.out.println("Column 'amount' not found");
                return;
            }
            
            // Read data lines and sum the amounts
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length > amountIndex) {
                    try {
                        sum += Double.parseDouble(values[amountIndex].trim());
                    } catch (NumberFormatException e) {
                        System.out.println("Skipping invalid amount value: " + values[amountIndex]);
                    }
                }
            }
            
            System.out.println("Sum of amount column: " + sum);
            
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
}
