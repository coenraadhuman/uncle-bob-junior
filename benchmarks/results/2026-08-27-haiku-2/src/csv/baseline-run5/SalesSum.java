import java.io.*;
import java.nio.file.*;
import java.util.*;

public class SalesSum {
    public static void main(String[] args) throws IOException {
        String filePath = "sales.csv";
        double totalAmount = 0;
        
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        
        if (lines.isEmpty()) {
            System.out.println("CSV file is empty");
            return;
        }
        
        // Parse header to find 'amount' column index
        String[] headers = lines.get(0).split(",");
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
        
        // Sum values from amount column
        for (int i = 1; i < lines.size(); i++) {
            String[] values = lines.get(i).split(",");
            if (values.length > amountIndex) {
                try {
                    totalAmount += Double.parseDouble(values[amountIndex].trim());
                } catch (NumberFormatException e) {
                    System.err.println("Invalid amount in row " + (i + 1) + ": " + values[amountIndex]);
                }
            }
        }
        
        System.out.println("Total Amount: " + totalAmount);
    }
}
