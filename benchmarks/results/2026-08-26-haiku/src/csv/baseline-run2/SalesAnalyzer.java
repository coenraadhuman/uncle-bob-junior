import java.io.*;
import java.util.*;

public class SalesAnalyzer {
    public static void main(String[] args) {
        String filename = "sales.csv";
        double total = 0.0;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            String[] headers = null;
            int amountColumnIndex = -1;
            boolean isFirstLine = true;
            
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                
                if (isFirstLine) {
                    headers = values;
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
                    isFirstLine = false;
                } else {
                    try {
                        double amount = Double.parseDouble(values[amountColumnIndex].trim());
                        total += amount;
                    } catch (NumberFormatException e) {
                        System.err.println("Warning: Could not parse value '" + values[amountColumnIndex] + "' as a number");
                    }
                }
            }
            
            System.out.println("Total amount: " + total);
            
        } catch (FileNotFoundException e) {
            System.err.println("Error: File '" + filename + "' not found");
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
}
