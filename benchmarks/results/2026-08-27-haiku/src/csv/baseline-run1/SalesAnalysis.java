import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SalesAnalysis {
    public static void main(String[] args) {
        String csvFile = "sales.csv";
        double sum = 0.0;
        int amountColumnIndex = -1;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
            String line;
            boolean isFirstLine = true;
            
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                
                if (isFirstLine) {
                    // Find the index of the 'amount' column
                    for (int i = 0; i < values.length; i++) {
                        if (values[i].trim().equalsIgnoreCase("amount")) {
                            amountColumnIndex = i;
                            break;
                        }
                    }
                    isFirstLine = false;
                } else if (amountColumnIndex != -1) {
                    // Parse and sum the amount values
                    try {
                        double amount = Double.parseDouble(values[amountColumnIndex].trim());
                        sum += amount;
                    } catch (NumberFormatException e) {
                        System.err.println("Warning: Invalid number in row: " + line);
                    }
                }
            }
            
            System.out.println("Total amount: " + sum);
            
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
}
