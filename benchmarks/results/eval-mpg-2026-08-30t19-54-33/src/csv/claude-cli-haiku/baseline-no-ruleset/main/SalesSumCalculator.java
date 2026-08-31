import java.io.*;

public class SalesSumCalculator {
    public static void main(String[] args) {
        String filePath = "sales.csv";
        double sum = 0;
        int amountColumnIndex = -1;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isHeader = true;
            
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                
                if (isHeader) {
                    for (int i = 0; i < values.length; i++) {
                        if (values[i].trim().equalsIgnoreCase("amount")) {
                            amountColumnIndex = i;
                            break;
                        }
                    }
                    isHeader = false;
                } else if (amountColumnIndex >= 0 && amountColumnIndex < values.length) {
                    try {
                        sum += Double.parseDouble(values[amountColumnIndex].trim());
                    } catch (NumberFormatException e) {
                        // Skip invalid entries
                    }
                }
            }
            
            System.out.println("Sum of amount column: " + sum);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
}
