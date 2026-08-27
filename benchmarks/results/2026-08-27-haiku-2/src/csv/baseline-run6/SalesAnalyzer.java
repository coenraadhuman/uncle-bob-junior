import java.io.*;

public class SalesAnalyzer {
    public static void main(String[] args) {
        String filePath = "sales.csv";
        double totalAmount = 0.0;
        
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isFirstLine = true;
            int amountColumnIndex = -1;
            
            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    // Parse header to find the 'amount' column index
                    String[] headers = line.split(",");
                    for (int i = 0; i < headers.length; i++) {
                        if (headers[i].trim().equalsIgnoreCase("amount")) {
                            amountColumnIndex = i;
                            break;
                        }
                    }
                    isFirstLine = false;
                } else {
                    // Parse data rows
                    String[] values = line.split(",");
                    if (amountColumnIndex >= 0 && amountColumnIndex < values.length) {
                        try {
                            double amount = Double.parseDouble(values[amountColumnIndex].trim());
                            totalAmount += amount;
                        } catch (NumberFormatException e) {
                            System.err.println("Warning: Could not parse value: " + values[amountColumnIndex]);
                        }
                    }
                }
            }
            
            System.out.println("Total Amount: " + totalAmount);
        } catch (FileNotFoundException e) {
            System.err.println("Error: File not found - " + filePath);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
}
