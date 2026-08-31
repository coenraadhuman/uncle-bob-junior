import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

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
            
            // Read data rows and sum the amount column
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length > amountIndex) {
                    try {
                        total += Double.parseDouble(values[amountIndex].trim());
                    } catch (NumberFormatException e) {
                        System.err.println("Skipping invalid value: " + values[amountIndex]);
                    }
                }
            }
            
            System.out.println("Sum of amount column: " + total);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
}
