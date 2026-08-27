import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class SalesTotal {

    private static final String SALES_CSV_PATH = "sales.csv";
    private static final String COLUMN_DELIMITER = ",";
    private static final String AMOUNT_COLUMN_NAME = "amount";

    public static void main(String[] args) throws IOException {
        List<String> lines = Files.readAllLines(Path.of(SALES_CSV_PATH));
        System.out.println(sumAmountColumn(lines));
    }

    static double sumAmountColumn(List<String> lines) {
        if (lines.isEmpty()) {
            return 0.0;
        }
        int amountColumnIndex = findAmountColumnIndex(lines.get(0));
        double total = 0.0;
        for (String line : lines.subList(1, lines.size())) {
            if (line.isBlank()) {
                continue;
            }
            total += parseAmount(line, amountColumnIndex);
        }
        return total;
    }

    private static int findAmountColumnIndex(String headerLine) {
        String[] headers = headerLine.split(COLUMN_DELIMITER);
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].trim().equalsIgnoreCase(AMOUNT_COLUMN_NAME)) {
                return i;
            }
        }
        throw new IllegalArgumentException("CSV is missing an '" + AMOUNT_COLUMN_NAME + "' column");
    }

    private static double parseAmount(String line, int amountColumnIndex) {
        String[] fields = line.split(COLUMN_DELIMITER);
        return Double.parseDouble(fields[amountColumnIndex].trim());
    }
}
