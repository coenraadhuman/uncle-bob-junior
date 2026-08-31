import java.io.*;

public class SalesAnalyzer {
    public static void main(String[] args) throws IOException {
        double sum = 0.0;
        int amountColumnIndex = -1;
        boolean isFirstLine = true;
        
        try (BufferedReader reader = new BufferedReader(new FileReader("sales.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                
                if (isFirstLine) {
                    // Find the index of the 'amount' column
                    for (int i = 0; i < fields.length; i++) {
                        if (fields[i].trim().equalsIgnoreCase("amount")) {
                            amountColumnIndex = i;
                            break;
                        }
                    }
                    isFirstLine = false;
                } else {
                    // Add values from the amount column
                    if (amountColumnIndex >= 0 && amountColumnIndex < fields.length) {
                        try {
                            sum += Double.parseDouble(fields[amountColumnIndex].trim());
                        } catch (NumberFormatException e) {
                            // Skip invalid numbers
                        }
                    }
                }
            }
        }
        
        System.out.println("Total Amount: " + sum);
    }
}
