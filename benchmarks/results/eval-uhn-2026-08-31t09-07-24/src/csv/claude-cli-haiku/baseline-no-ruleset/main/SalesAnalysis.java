import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SalesAnalysis {
    public static void main(String[] args) {
        double sum = 0.0;
        
        try (BufferedReader reader = new BufferedReader(new FileReader("sales.csv"))) {
            String line;
            String[] headers = null;
            int amountIndex = -1;
            
            // Read header line
            if ((line = reader.readLine()) != null) {
                headers = line.split(",");
                // Find the index of 'amount' column
                for (int i = 0; i < headers.length; i++) {
                    if (headers[i].trim().equalsIgnoreCase("amount")) {
                        amountIndex = i;
                        break;
                    }
                }
                
                if (amountIndex == -1) {
                    System.err.println("Error: 'amount' column not found");
                    return;
                }
            }
            
            // Read data lines
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length > amountIndex) {
                    try {
                        double amount = Double.parseDouble(values[amountIndex].trim());
                        sum += amount;
                    } catch (NumberFormatException e) {
                        System.err.println("Warning: Could not parse amount value: " + values[amountIndex]);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("Sum of amount column: " + sum);
    }
}
