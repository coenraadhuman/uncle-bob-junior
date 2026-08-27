import java.io.*;
import java.util.*;

public class SalesSum {
    public static void main(String[] args) {
        String filePath = "sales.csv";
        double total = 0;
        int amountColumnIndex = -1;

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isHeader = true;

            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");

                if (isHeader) {
                    amountColumnIndex = findColumnIndex(fields, "amount");
                    if (amountColumnIndex == -1) {
                        System.err.println("Error: 'amount' column not found in CSV header.");
                        return;
                    }
                    isHeader = false;
                    continue;
                }

                if (amountColumnIndex < fields.length) {
                    try {
                        total += Double.parseDouble(fields[amountColumnIndex].trim());
                    } catch (NumberFormatException e) {
                        System.err.println("Warning: Invalid number in amount column: " + fields[amountColumnIndex]);
                    }
                }
            }

            System.out.println("Total amount: " + total);
        } catch (FileNotFoundException e) {
            System.err.println("Error: File not found: " + filePath);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
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
