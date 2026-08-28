import java.io.*;
import java.util.*;

public class SalesSum {
    public static void main(String[] args) {
        double sum = 0.0;
        int amountColumnIndex = -1;
        
        try (BufferedReader reader = new BufferedReader(new FileReader("sales.csv"))) {
            String line;
            boolean isHeader = true;
            
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                
                if (isHeader) {
                    for (int i = 0; i < fields.length; i++) {
                        if (fields[i].trim().equalsIgnoreCase("amount")) {
                            amountColumnIndex = i;
                            break;
                        }
                    }
                    if (amountColumnIndex == -1) {
                        System.err.println("Error: 'amount' column not found");
                        return;
                    }
                    isHeader = false;
                } else {
                    try {
                        double amount = Double.parseDouble(fields[amountColumnIndex].trim());
                        sum += amount;
                    } catch (NumberFormatException e) {
                        System.err.println("Warning: Could not parse amount value: " + fields[amountColumnIndex]);
                    }
                }
            }
            
            System.out.println("Sum of amount column: " + sum);
            
        } catch (FileNotFoundException e) {
            System.err.println("Error: sales.csv not found");
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
}
