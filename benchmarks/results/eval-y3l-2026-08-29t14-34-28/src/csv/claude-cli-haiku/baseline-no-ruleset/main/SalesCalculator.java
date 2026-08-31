import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SalesCalculator {
    public static void main(String[] args) {
        String fileName = "sales.csv";
        double total = 0.0;
        int amountColumnIndex = -1;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            boolean isHeader = true;
            
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                
                if (isHeader) {
                    // Find the index of the "amount" column
                    for (int i = 0; i < values.length; i++) {
                        if (values[i].trim().equalsIgnoreCase("amount")) {
                            amountColumnIndex = i;
                            break;
                        }
                    }
                    isHeader = false;
                } else {
                    // Sum the amount column
                    if (amountColumnIndex >= 0 && amountColumnIndex < values.length) {
                        try {
                            total += Double.parseDouble(values[amountColumnIndex].trim());
                        } catch (NumberFormatException e) {
                            System.err.println("Warning: Could not parse amount: " + values[amountColumnIndex]);
                        }
                    }
                }
            }
            
            System.out.println("Total Amount: " + total);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
}
