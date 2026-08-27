import java.io.*;
import java.nio.file.*;
import java.util.*;

public class SalesSum {
    public static void main(String[] args) throws IOException {
        String filePath = "sales.csv";
        double total = 0;
        int amountColumnIndex = -1;
        boolean isFirstLine = true;

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");

                if (isFirstLine) {
                    // Find the index of the 'amount' column
                    for (int i = 0; i < values.length; i++) {
                        if (values[i].trim().equalsIgnoreCase("amount")) {
                            amountColumnIndex = i;
                            break;
                        }
                    }
                    isFirstLine = false;
                } else {
                    // Sum the amount column
                    if (amountColumnIndex >= 0 && amountColumnIndex < values.length) {
                        try {
                            total += Double.parseDouble(values[amountColumnIndex].trim());
                        } catch (NumberFormatException e) {
                            System.err.println("Warning: Invalid number in amount column: " + values[amountColumnIndex]);
                        }
                    }
                }
            }
        }

        System.out.println("Total amount: " + total);
    }
}
