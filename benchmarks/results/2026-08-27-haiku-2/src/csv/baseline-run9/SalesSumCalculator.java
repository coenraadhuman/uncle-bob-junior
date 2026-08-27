import java.io.*;
import java.util.*;

public class SalesSumCalculator {
    public static void main(String[] args) {
        double total = 0.0;
        
        try (BufferedReader reader = new BufferedReader(new FileReader("sales.csv"))) {
            String line;
            String[] headers = null;
            int amountIndex = -1;
            
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                
                // First line: find the index of the 'amount' column
                if (headers == null) {
                    headers = values;
                    for (int i = 0; i < headers.length; i++) {
                        if (headers[i].trim().equalsIgnoreCase("amount")) {
                            amountIndex = i;
                            break;
                        }
                    }
                    if (amountIndex == -1) {
                        System.err.println("Error: 'amount' column not found");
                        return;
                    }
                } else {
                    // Data row: add to sum
                    try {
                        total += Double.parseDouble(values[amountIndex].trim());
                    } catch (NumberFormatException e) {
                        System.err.println("Warning: Skipping invalid amount value: " + values[amountIndex]);
                    }
                }
            }
            
            System.out.println("Total amount: " + total);
            
        } catch (FileNotFoundException e) {
            System.err.println("Error: sales.csv file not found");
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
}
