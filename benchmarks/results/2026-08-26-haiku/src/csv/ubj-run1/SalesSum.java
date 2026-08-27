import java.io.*;
import java.util.*;

public class SalesSum {
    public static void main(String[] args) {
        String filePath = "sales.csv";
        double totalAmount = 0;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                System.out.println("CSV file is empty");
                return;
            }
            
            int amountColumnIndex = findColumnIndex(headerLine, "amount");
            if (amountColumnIndex == -1) {
                System.out.println("Column 'amount' not found in CSV");
                return;
            }
            
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                if (amountColumnIndex < values.length) {
                    try {
                        double amount = Double.parseDouble(values[amountColumnIndex].trim());
                        totalAmount += amount;
                    } catch (NumberFormatException e) {
                        System.out.println("Skipping invalid amount value: " + values[amountColumnIndex]);
                    }
                }
            }
            
            System.out.println("Sum of amount column: " + totalAmount);
            
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + filePath);
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }
    
    private static int findColumnIndex(String headerLine, String columnName) {
        String[] headers = headerLine.split(",");
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].trim().equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        return -1;
    }
}
