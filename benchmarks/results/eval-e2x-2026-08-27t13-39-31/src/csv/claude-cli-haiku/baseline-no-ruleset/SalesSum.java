import java.io.*;

public class SalesSum {
    public static void main(String[] args) {
        String filename = "sales.csv";
        double total = 0.0;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            String[] headers = null;
            int amountIndex = -1;
            
            // Read header row to find amount column
            if ((line = reader.readLine()) != null) {
                headers = line.split(",");
                for (int i = 0; i < headers.length; i++) {
                    if (headers[i].trim().equalsIgnoreCase("amount")) {
                        amountIndex = i;
                        break;
                    }
                }
            }
            
            if (amountIndex == -1) {
                System.err.println("'amount' column not found");
                return;
            }
            
            // Read data rows and sum amounts
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                if (amountIndex < values.length) {
                    try {
                        total += Double.parseDouble(values[amountIndex].trim());
                    } catch (NumberFormatException e) {
                        System.err.println("Skipping invalid amount: " + values[amountIndex]);
                    }
                }
            }
            
            System.out.println("Total amount: " + total);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
}
