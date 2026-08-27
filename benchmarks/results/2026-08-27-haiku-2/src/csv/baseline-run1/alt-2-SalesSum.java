import com.opencsv.CSVReader;
import java.io.*;

public class SalesSum {
    public static void main(String[] args) throws IOException {
        String filePath = "sales.csv";
        double total = 0;
        int amountColumnIndex = -1;
        boolean isFirstLine = true;

        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            String[] line;
            while ((line = reader.readNext()) != null) {
                if (isFirstLine) {
                    for (int i = 0; i < line.length; i++) {
                        if (line[i].equalsIgnoreCase("amount")) {
                            amountColumnIndex = i;
                            break;
                        }
                    }
                    isFirstLine = false;
                } else {
                    if (amountColumnIndex >= 0 && amountColumnIndex < line.length) {
                        try {
                            total += Double.parseDouble(line[amountColumnIndex].trim());
                        } catch (NumberFormatException e) {
                            System.err.println("Warning: Invalid number: " + line[amountColumnIndex]);
                        }
                    }
                }
            }
        }

        System.out.println("Total amount: " + total);
    }
}
