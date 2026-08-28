import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Prints the sum of the 'amount' column of sales.csv.
 * Expects a header row; blank lines are skipped.
 * ubj: naive split-based CSV, replace with commons-csv if fields ever contain quoted commas.
 */
public final class SalesTotal {

    private static final Path SALES_FILE = Path.of("sales.csv");
    private static final String AMOUNT_COLUMN = "amount";
    private static final String DELIMITER = ",";
    private static final int HEADER_ROW_COUNT = 1;

    public static void main(String[] args) throws IOException {
        List<String> lines = Files.readAllLines(SALES_FILE);
        System.out.println(sumAmountColumn(lines));
    }

    static BigDecimal sumAmountColumn(List<String> csvLines) {
        if (csvLines.isEmpty()) {
            throw new IllegalArgumentException("CSV is empty: a header row is required");
        }
        int amountIndex = columnIndex(csvLines.get(0), AMOUNT_COLUMN);
        return csvLines.stream()
                .skip(HEADER_ROW_COUNT)
                .filter(line -> !line.isBlank())
                .map(line -> amountIn(line, amountIndex))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static int columnIndex(String headerLine, String columnName) {
        String[] headers = headerLine.split(DELIMITER, -1);
        for (int index = 0; index < headers.length; index++) {
            if (headers[index].trim().equalsIgnoreCase(columnName)) {
                return index;
            }
        }
        throw new IllegalArgumentException(
                "Missing '" + columnName + "' column in header: " + headerLine);
    }

    private static BigDecimal amountIn(String csvLine, int amountIndex) {
        String[] fields = csvLine.split(DELIMITER, -1);
        if (amountIndex >= fields.length) {
            throw new IllegalArgumentException("Row has no amount field: " + csvLine);
        }
        return new BigDecimal(fields[amountIndex].trim());
    }

    private SalesTotal() {
    }
}
