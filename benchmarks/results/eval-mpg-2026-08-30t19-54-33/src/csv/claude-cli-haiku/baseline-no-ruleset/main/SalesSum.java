import java.io.*;

public class SalesSum {
    public static void main(String[] args) {
        String filePath = "sales.csv";
        double sum = 0.0;
        int amountColumnIndex = -1;
        boolean isFirstLine = true;

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                
                if (isFirstLine) {
                    // Find the index of 'amount' column
                    for (int i = 0; i < values.length; i++) {
                        if (values[i].trim().equalsIgnoreCase("amount")) {
                            amountColumnIndex = i;
                            break;
                        }
                    }
                    if (amountColumnIndex == -1) {
                        System.out.println("Error: 'amount' column not found");
                        return;
                    }
                    isFirstLine = false;
                } else {
                    // Parse and sum the amount value
                    try {
                        double amount = Double.parseDouble(values[amountColumnIndex].trim());
                        sum += amount;
                    } catch (NumberFormatException e) {
                        System.out.println("Warning: Invalid number in row: " + line);
                    }
                }
            }
            
            System.out.println("Sum of amount column: " + sum);
            
        } catch (FileNotFoundException e) {
            System.out.println("Error: File not found - " + filePath);
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
