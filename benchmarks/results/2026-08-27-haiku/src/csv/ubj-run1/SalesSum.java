import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SalesSum {
    private static final String CSV_FILE = "sales.csv";
    private static final int AMOUNT_COLUMN_INDEX = 1; // Assumes 'amount' is the 2nd column

    public static void main(String[] args) {
        try {
            double totalAmount = sumAmountColumn();
            System.out.println("Total Amount: " + totalAmount);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }

    private static double sumAmountColumn() throws IOException {
        double sum = 0.0;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(CSV_FILE))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return sum;
            }
            
            String[] headers = headerLine.split(",");
            int amountIndex = findColumnIndex(headers, "amount");
            
            if (amountIndex == -1) {
                throw new IOException("'amount' column not found in CSV");
            }
            
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                if (amountIndex < values.length) {
                    try {
                        sum += Double.parseDouble(values[amountIndex].trim());
                    } catch (NumberFormatException e) {
                        System.err.println("Skipping invalid amount: " + values[amountIndex]);
                    }
                }
            }
        }
        
        return sum;
    }

    private static int findColumnIndex(String[] headers, String columnName) {
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].trim().equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        return -1;
    }
}
