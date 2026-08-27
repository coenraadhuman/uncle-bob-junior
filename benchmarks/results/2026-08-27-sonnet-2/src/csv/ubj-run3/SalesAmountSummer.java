import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class SalesAmountSummer {

    private static final String COLUMN_SEPARATOR = ",";
    private static final String AMOUNT_COLUMN_NAME = "amount";
    private static final String DEFAULT_CSV_PATH = "sales.csv";

    private SalesAmountSummer() {
    }

    public static void main(String[] args) throws IOException {
        Path csvPath = Path.of(args.length > 0 ? args[0] : DEFAULT_CSV_PATH);
        BigDecimal total = sumAmountColumn(csvPath);
        System.out.println(total);
    }

    public static BigDecimal sumAmountColumn(Path csvPath) throws IOException {
        List<String> lines = Files.readAllLines(csvPath);
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("CSV file is empty: " + csvPath);
        }

        int amountIndex = findColumnIndex(lines.get(0), AMOUNT_COLUMN_NAME);

        BigDecimal total = BigDecimal.ZERO;
        for (String line : lines.subList(1, lines.size())) {
            if (line.isBlank()) {
                continue;
            }
            total = total.add(parseAmount(line, amountIndex));
        }
        return total;
    }

    private static int findColumnIndex(String headerLine, String columnName) {
        String[] headers = headerLine.split(COLUMN_SEPARATOR);
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].trim().equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Column '" + columnName + "' not found in header: " + headerLine);
    }

    private static BigDecimal parseAmount(String dataLine, int amountIndex) {
        String[] fields = dataLine.split(COLUMN_SEPARATOR);
        if (amountIndex >= fields.length) {
            throw new IllegalArgumentException("Row has no value at amount column: " + dataLine);
        }
        return new BigDecimal(fields[amountIndex].trim());
    }
}
