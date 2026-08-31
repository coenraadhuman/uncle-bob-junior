import java.io.*;
import java.util.*;

public class SalesSum {
    public static void main(String[] args) {
        String filename = "sales.csv";
        double total = 0;
        int amountColumnIndex = -1;

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
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
                } else {
                    if (amountColumnIndex >= 0 && amountColumnIndex < values.length) {
                        try {
                            total += Double.parseDouble(values[amountColumnIndex].trim());
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid number: " + values[amountColumnIndex]);
                        }
                    }
                }
            }

            System.out.println("Sum of amount column: " + total);

        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + filename);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
}
