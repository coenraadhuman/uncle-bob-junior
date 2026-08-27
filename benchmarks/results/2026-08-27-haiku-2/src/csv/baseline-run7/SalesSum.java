import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SalesSum {
    public static void main(String[] args) {
        String filePath = "sales.csv";
        double total = 0;
        int amountColumnIndex = -1;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean headerProcessed = false;
            
            while ((line = reader.readLine()) != null) {
                if (!headerProcessed) {
                    // Parse header to find amount column
                    String[] headers = line.split(",");
                    for (int i = 0; i < headers.length; i++) {
                        if (headers[i].trim().equalsIgnoreCase("amount")) {
                            amountColumnIndex = i;
                            break;
                        }
                    }
                    
                    if (amountColumnIndex == -1) {
                        System.err.println("Error: 'amount' column not found in CSV");
                        return;
                    }
                    headerProcessed = true;
                } else {
                    // Parse data rows
                    String[] values = line.split(",");
                    if (amountColumnIndex < values.length) {
                        try {
                            double amount = Double.parseDouble(values[amountColumnIndex].trim());
                            total += amount;
                        } catch (NumberFormatException e) {
                            System.err.println("Warning: Could not parse value: " + values[amountColumnIndex]);
                        }
                    }
                }
            }
            
            System.out.println("Sum of amount column: " + total);
            
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
