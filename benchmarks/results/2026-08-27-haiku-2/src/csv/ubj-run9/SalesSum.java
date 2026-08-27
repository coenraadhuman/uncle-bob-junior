import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SalesSum {
    public static void main(String[] args) {
        String filePath = "sales.csv";
        double total = 0.0;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line = reader.readLine();
            if (line == null) {
                System.out.println("File is empty");
                return;
            }
            
            String[] headers = line.split(",");
            int amountColumnIndex = -1;
            for (int i = 0; i < headers.length; i++) {
                if (headers[i].trim().equalsIgnoreCase("amount")) {
                    amountColumnIndex = i;
                    break;
                }
            }
            
            if (amountColumnIndex == -1) {
                System.out.println("'amount' column not found");
                return;
            }
            
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length > amountColumnIndex) {
                    try {
                        total += Double.parseDouble(values[amountColumnIndex].trim());
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid number format: " + values[amountColumnIndex]);
                    }
                }
            }
            
            System.out.printf("Sum of 'amount' column: %.2f%n", total);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
}
