import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;

public class SalesSum {
    public static void main(String[] args) {
        try {
            BigDecimal sum = sumAmountColumn("sales.csv");
            System.out.println("Total amount: " + sum);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }

    private static BigDecimal sumAmountColumn(String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String header = reader.readLine();
            if (header == null) {
                return BigDecimal.ZERO;
            }

            int amountIndex = findColumnIndex(header, "amount");
            if (amountIndex == -1) {
                throw new IllegalArgumentException("Column 'amount' not found in CSV");
            }

            BigDecimal sum = BigDecimal.ZERO;
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                if (amountIndex < fields.length) {
                    try {
                        sum = sum.add(new BigDecimal(fields[amountIndex].trim()));
                    } catch (NumberFormatException e) {
                        System.err.println("Skipping invalid amount: " + fields[amountIndex]);
                    }
                }
            }
            return sum;
        }
    }

    private static int findColumnIndex(String header, String columnName) {
        String[] columns = header.split(",");
        for (int i = 0; i < columns.length; i++) {
            if (columns[i].trim().equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        return -1;
    }
}
