import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class SalesTotal {

    private static final Path SALES_FILE = Path.of("sales.csv");
    private static final String AMOUNT_COLUMN = "amount";
    private static final String DELIMITER = ",";

    public static void main(String[] args) {
        List<String> lines = readLines(SALES_FILE);
        if (lines.isEmpty()) {
            System.out.println(BigDecimal.ZERO);
            return;
        }

        int amountIndex = findColumnIndex(lines.get(0), AMOUNT_COLUMN);
        BigDecimal total = sumColumn(lines.subList(1, lines.size()), amountIndex);
        System.out.println(total);
    }

    private static List<String> readLines(Path file) {
        try {
            return Files.readAllLines(file);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read " + file, e);
        }
    }

    private static int findColumnIndex(String headerLine, String columnName) {
        String[] headers = headerLine.split(DELIMITER);
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].trim().equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Missing column: " + columnName);
    }

    private static BigDecimal sumColumn(List<String> dataLines, int columnIndex) {
        BigDecimal total = BigDecimal.ZERO;
        for (String line : dataLines) {
            if (line.isBlank()) {
                continue;
            }
            total = total.add(parseAmount(line, columnIndex));
        }
        return total;
    }

    private static BigDecimal parseAmount(String line, int columnIndex) {
        String[] fields = line.split(DELIMITER, -1);
        if (columnIndex >= fields.length) {
            throw new IllegalArgumentException("Row has no amount field: " + line);
        }
        return new BigDecimal(fields[columnIndex].trim());
    }
}
