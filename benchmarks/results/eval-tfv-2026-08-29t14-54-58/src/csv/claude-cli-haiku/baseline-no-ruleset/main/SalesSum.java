import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SalesSum {
    public static void main(String[] args) {
        String fileName = "sales.csv";
        double total = 0;
        int amountColumnIndex = -1;

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            boolean isHeader = true;

            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");

                if (isHeader) {
                    for (int i = 0; i < fields.length; i++) {
                        if (fields[i].trim().equalsIgnoreCase("amount")) {
                            amountColumnIndex = i;
                            break;
                        }
                    }
                    isHeader = false;
                    continue;
                }

                if (amountColumnIndex != -1 && amountColumnIndex < fields.length) {
                    try {
                        total += Double.parseDouble(fields[amountColumnIndex].trim());
                    } catch (NumberFormatException e) {
                        System.err.println("Warning: Could not parse amount value: " + fields[amountColumnIndex]);
                    }
                }
            }

            System.out.println("Sum of amount column: " + total);

        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
}
