// SalesAmountSummer.java
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class SalesAmountSummer {

    private static final String CSV_DELIMITER = ",";
    private static final String AMOUNT_COLUMN_NAME = "amount";
    private static final Path SALES_CSV_PATH = Path.of("sales.csv");

    public static void main(String[] args) throws IOException {
        List<String> lines = Files.readAllLines(SALES_CSV_PATH);
        double totalAmount = sumAmountColumn(lines);
        System.out.printf("Total amount: %.2f%n", totalAmount);
    }

    static double sumAmountColumn(List<String> lines) {
        if (lines.isEmpty()) {
            return 0.0;
        }
        int amountColumnIndex = findAmountColumnIndex(lines.get(0));
        double total = 0.0;
        for (String row : lines.subList(1, lines.size())) {
            total += parseAmount(row, amountColumnIndex);
        }
        return total;
    }

    private static int findAmountColumnIndex(String headerLine) {
        String[] columnNames = headerLine.split(CSV_DELIMITER);
        for (int i = 0; i < columnNames.length; i++) {
            if (columnNames[i].trim().equalsIgnoreCase(AMOUNT_COLUMN_NAME)) {
                return i;
            }
        }
        throw new IllegalArgumentException("CSV is missing an 'amount' column");
    }

    private static double parseAmount(String row, int amountColumnIndex) {
        String[] values = row.split(CSV_DELIMITER);
        return Double.parseDouble(values[amountColumnIndex].trim());
    }
}
