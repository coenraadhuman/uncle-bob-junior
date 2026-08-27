import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public class SalesAnalyzer {
    public static void main(String[] args) {
        try {
            double totalAmount = sumSalesAmount("sales.csv");
            System.out.println("Total Amount: " + totalAmount);
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    private static double sumSalesAmount(String filePath) throws IOException {
        double total = 0.0;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IOException("CSV file is empty");
            }
            
            String[] headers = headerLine.split(",");
            int amountIndex = Arrays.asList(headers).indexOf("amount");
            
            if (amountIndex == -1) {
                throw new IOException("'amount' column not found in CSV");
            }
            
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length > amountIndex) {
                    total += Double.parseDouble(values[amountIndex].trim());
                }
            }
        }
        
        return total;
    }
}
