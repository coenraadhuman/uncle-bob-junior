import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SalesSumCalculator {
    public static void main(String[] args) {
        try (BufferedReader reader = new BufferedReader(new FileReader("sales.csv"))) {
            // Read header line and find 'amount' column index
            String[] headers = reader.readLine().split(",");
            int amountIndex = -1;
            for (int i = 0; i < headers.length; i++) {
                if (headers[i].trim().equals("amount")) {
                    amountIndex = i;
                    break;
                }
            }
            
            if (amountIndex == -1) {
                System.err.println("'amount' column not found");
                return;
            }
            
            // Sum the amount column
            double sum = 0.0;
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length > amountIndex) {
                    sum += Double.parseDouble(values[amountIndex].trim());
                }
            }
            
            System.out.println(sum);
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
