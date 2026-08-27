import java.io.*;
import java.util.*;

public class SalesSum {
    public static void main(String[] args) {
        String filePath = "sales.csv";
        double total = 0.0;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line = reader.readLine();
            if (line == null) {
                System.out.println("File is empty");
                return;
            }
            
            // Parse header to find 'amount' column index
            String[] headers = line.split(",");
            int amountIndex = -1;
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
            
            // Read data rows and sum amounts
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length > amountIndex) {
                    try {
                        double amount = Double.parseDouble(values[amountIndex].trim());
                        total += amount;
                    } catch (NumberFormatException e) {
                        // Skip rows with invalid amounts
                    }
                }
            }
            
            System.out.println(total);
            
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + filePath);
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }
}
