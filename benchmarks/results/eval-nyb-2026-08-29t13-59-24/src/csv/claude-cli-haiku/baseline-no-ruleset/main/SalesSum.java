import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SalesSum {
    public static void main(String[] args) {
        double total = 0;
        int amountColumnIndex = -1;
        
        try (BufferedReader reader = new BufferedReader(new FileReader("sales.csv"))) {
            String line;
            boolean isFirstLine = true;
            
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                
                // Find the 'amount' column index from the header
                if (isFirstLine) {
                    for (int i = 0; i < parts.length; i++) {
                        if (parts[i].trim().equalsIgnoreCase("amount")) {
                            amountColumnIndex = i;
                            break;
                        }
                    }
                    isFirstLine = false;
                    continue;
                }
                
                // Sum the amount column
                if (amountColumnIndex >= 0 && amountColumnIndex < parts.length) {
                    try {
                        total += Double.parseDouble(parts[amountColumnIndex].trim());
                    } catch (NumberFormatException e) {
                        System.err.println("Warning: Could not parse value: " + parts[amountColumnIndex]);
                    }
                }
            }
            
            System.out.println("Total: " + total);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
}
