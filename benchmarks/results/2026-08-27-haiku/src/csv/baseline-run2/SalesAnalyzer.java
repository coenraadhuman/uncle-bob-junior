import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SalesAnalyzer {
    public static void main(String[] args) {
        String filePath = "sales.csv";
        double total = 0.0;
        
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            int amountColumnIndex = -1;
            int lineNumber = 0;
            
            while ((line = br.readLine()) != null) {
                if (lineNumber == 0) {
                    // Parse header row to find 'amount' column index
                    String[] headers = line.split(",");
                    for (int i = 0; i < headers.length; i++) {
                        if (headers[i].trim().equalsIgnoreCase("amount")) {
                            amountColumnIndex = i;
                            break;
                        }
                    }
                    if (amountColumnIndex == -1) {
                        System.err.println("Error: 'amount' column not found");
                        return;
                    }
                } else {
                    // Parse data rows and sum the amount column
                    String[] values = line.split(",");
                    if (amountColumnIndex < values.length) {
                        try {
                            total += Double.parseDouble(values[amountColumnIndex].trim());
                        } catch (NumberFormatException e) {
                            System.err.println("Warning: Invalid number on line " + (lineNumber + 1));
                        }
                    }
                }
                lineNumber++;
            }
            
            System.out.println("Sum of amount column: " + total);
            
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
