import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class SalesAmountSummary {

    private static final String DEFAULT_CSV_FILE_NAME = "sales.csv";
    private static final String COLUMN_DELIMITER = ",";
    private static final String AMOUNT_COLUMN_NAME = "amount";

    private SalesAmountSummary() {
    }

    public static void main(String[] args) throws IOException {
        Path csvPath = Path.of(args.length > 0 ? args[0] : DEFAULT_CSV_FILE_NAME);
        BigDecimal total = sumAmountColumn(csvPath);
        System.out.println(total);
    }

    static BigDecimal sumAmountColumn(Path csvPath) throws IOException {
        List<String> lines = Files.readAllLines(csvPath);
        if (lines.isEmpty()) {
            return BigDecimal.ZERO;
        }

        int amountColumnIndex = findAmountColumnIndex(lines.get(0));
        BigDecimal total = BigDecimal.ZERO;
        for (String line : lines.subList(1, lines.size())) {
            total = total.add(parseAmount(line, amountColumnIndex));
        }
        return total;
    }

    private static int findAmountColumnIndex(String headerLine) {
        String[] headers = headerLine.split(COLUMN_DELIMITER);
        for (int index = 0; index < headers.length; index++) {
            if (headers[index].trim().equalsIgnoreCase(AMOUNT_COLUMN_NAME)) {
                return index;
            }
        }
        throw new IllegalArgumentException("CSV header is missing an '" + AMOUNT_COLUMN_NAME + "' column");
    }

    private static BigDecimal parseAmount(String line, int amountColumnIndex) {
        if (line.isBlank()) {
            return BigDecimal.ZERO;
        }
        String[] fields = line.split(COLUMN_DELIMITER);
        return new BigDecimal(fields[amountColumnIndex].trim());
    }
}
