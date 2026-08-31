import java.io.*;
import java.util.*;

public class SalesAnalyzer {
    public static void main(String[] args) {
        double sum = 0.0;
        
        try (Scanner scanner = new Scanner(new File("sales.csv"))) {
            if (!scanner.hasNextLine()) {
                System.out.println("File is empty");
                return;
            }
            
            // Parse header to find 'amount' column index
            String[] headers = scanner.nextLine().split(",");
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
            
            // Sum the amounts
            while (scanner.hasNextLine()) {
                String[] values = scanner.nextLine().split(",");
                if (values.length > amountIndex) {
                    try {
                        sum += Double.parseDouble(values[amountIndex].trim());
                    } catch (NumberFormatException e) {
                        // Skip rows with invalid amounts
                    }
                }
            }
            
            System.out.println("Total: " + sum);
            
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + e.getMessage());
        }
    }
}
