import java.io.*;

public class SalesAnalyzer {
    public static void main(String[] args) {
        String filename = "sales.csv";
        double sum = 0;
        int amountColumnIndex = -1;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            int lineNumber = 0;
            
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                
                if (lineNumber == 0) {
                    // Find the index of the 'amount' column in the header
                    for (int i = 0; i < values.length; i++) {
                        if (values[i].trim().equalsIgnoreCase("amount")) {
                            amountColumnIndex = i;
                            break;
                        }
                    }
                    if (amountColumnIndex == -1) {
                        System.err.println("Error: 'amount' column not found");
                        return;
                    }
                } else {
                    // Parse and sum the amount values
                    try {
                        double amount = Double.parseDouble(values[amountColumnIndex].trim());
                        sum += amount;
                    } catch (NumberFormatException e) {
                        System.err.println("Warning: Could not parse value: " + values[amountColumnIndex]);
                    }
                }
                lineNumber++;
            }
        } catch (FileNotFoundException e) {
            System.err.println("Error: File not found - " + filename);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
        
        System.out.println("Sum of amount column: " + sum);
    }
}
