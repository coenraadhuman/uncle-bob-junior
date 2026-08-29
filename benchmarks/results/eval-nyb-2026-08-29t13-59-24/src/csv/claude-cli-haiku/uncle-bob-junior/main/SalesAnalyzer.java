import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class SalesAnalyzer {
    public static void main(String[] args) {
        double total = sumAmountColumn("sales.csv");
        System.out.println("Total amount: " + total);
    }

    static double sumAmountColumn(String filename) {
        double sum = 0;
        try (Scanner scanner = new Scanner(new File(filename))) {
            if (!scanner.hasNextLine()) {
                return sum;
            }

            String headerLine = scanner.nextLine();
            int amountIndex = findColumnIndex(headerLine, "amount");
            
            if (amountIndex == -1) {
                System.err.println("Error: 'amount' column not found");
                return sum;
            }

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] values = line.split(",");
                
                if (amountIndex < values.length) {
                    try {
                        sum += Double.parseDouble(values[amountIndex].trim());
                    } catch (NumberFormatException e) {
                        System.err.println("Warning: Skipping invalid value");
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("Error: File not found: " + filename);
        }
        return sum;
    }

    static int findColumnIndex(String headerLine, String columnName) {
        String[] headers = headerLine.split(",");
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].trim().equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        return -1;
    }
}
