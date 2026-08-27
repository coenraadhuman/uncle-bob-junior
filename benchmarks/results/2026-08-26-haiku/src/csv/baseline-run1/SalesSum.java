import java.io.*;

public class SalesSum {
    public static void main(String[] args) {
        String fileName = "sales.csv";
        double sum = 0;
        
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            
            // Read and parse header
            String header = reader.readLine();
            if (header == null) {
                System.out.println("Error: File is empty");
                return;
            }
            
            String[] headers = header.split(",");
            int amountIndex = -1;
            
            // Find the 'amount' column index
            for (int i = 0; i < headers.length; i++) {
                if (headers[i].trim().equalsIgnoreCase("amount")) {
                    amountIndex = i;
                    break;
                }
            }
            
            if (amountIndex == -1) {
                System.out.println("Error: 'amount' column not found");
                reader.close();
                return;
            }
            
            // Sum the amount values
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length > amountIndex) {
                    try {
                        sum += Double.parseDouble(values[amountIndex].trim());
                    } catch (NumberFormatException e) {
                        // Skip invalid numbers
                    }
                }
            }
            
            reader.close();
            System.out.println("Sum of amount column: " + sum);
            
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }
}
