import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SalesSum {
    public static void main(String[] args) {
        String filePath = "sales.csv";
        double totalAmount = 0;
        
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            String[] headers = null;
            int amountIndex = -1;
            
            // Read header line
            if ((line = br.readLine()) != null) {
                headers = line.split(",");
                // Find the index of 'amount' column
                for (int i = 0; i < headers.length; i++) {
                    if (headers[i].trim().equalsIgnoreCase("amount")) {
                        amountIndex = i;
                        break;
                    }
                }
                
                if (amountIndex == -1) {
                    System.err.println("'amount' column not found");
                    return;
                }
            }
            
            // Read data lines
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length > amountIndex) {
                    try {
                        double amount = Double.parseDouble(values[amountIndex].trim());
                        totalAmount += amount;
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid number: " + values[amountIndex]);
                    }
                }
            }
            
            System.out.println("Total amount: " + totalAmount);
            
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
}
