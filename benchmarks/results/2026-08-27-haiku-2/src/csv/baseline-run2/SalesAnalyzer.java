import java.io.*;

public class SalesAnalyzer {
    public static void main(String[] args) {
        double total = 0;
        
        try (BufferedReader reader = new BufferedReader(new FileReader("sales.csv"))) {
            String line;
            boolean isHeader = true;
            int amountIndex = -1;
            
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                
                if (isHeader) {
                    // Find the index of the 'amount' column
                    for (int i = 0; i < values.length; i++) {
                        if (values[i].trim().equalsIgnoreCase("amount")) {
                            amountIndex = i;
                            break;
                        }
                    }
                    isHeader = false;
                } else {
                    // Sum the amount column
                    if (amountIndex >= 0 && amountIndex < values.length) {
                        try {
                            total += Double.parseDouble(values[amountIndex].trim());
                        } catch (NumberFormatException e) {
                            System.err.println("Warning: Could not parse value: " + values[amountIndex]);
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
        
        System.out.println("Sum of amount column: " + total);
    }
}
