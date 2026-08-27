import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SalesAnalysis {
    public static void main(String[] args) {
        String filePath = "sales.csv";
        double totalAmount = 0;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            String[] headers = null;
            int amountIndex = -1;
            
            while ((line = reader.readLine()) != null) {
                if (headers == null) {
                    // Parse header row to find 'amount' column
                    headers = line.split(",");
                    for (int i = 0; i < headers.length; i++) {
                        if (headers[i].trim().equalsIgnoreCase("amount")) {
                            amountIndex = i;
                            break;
                        }
                    }
                    if (amountIndex == -1) {
                        System.out.println("'amount' column not found");
                        return;
                    }
                } else {
                    // Parse data rows
                    String[] values = line.split(",");
                    if (amountIndex < values.length) {
                        try {
                            double amount = Double.parseDouble(values[amountIndex].trim());
                            totalAmount += amount;
                        } catch (NumberFormatException e) {
                            System.out.println("Skipping invalid amount: " + values[amountIndex]);
                        }
                    }
                }
            }
            
            System.out.println("Sum of amount column: " + totalAmount);
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }
}
