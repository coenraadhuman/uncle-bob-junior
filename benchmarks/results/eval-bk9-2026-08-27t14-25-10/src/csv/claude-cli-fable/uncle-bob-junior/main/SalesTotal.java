import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class SalesTotal {

    private static final Path SALES_FILE = Path.of("sales.csv");
    private static final String COLUMN_SEPARATOR = ",";
    private static final String AMOUNT_COLUMN = "amount";

    public static void main(String[] args) throws IOException {
        List<String> lines = Files.readAllLines(SALES_FILE);
        System.out.println(sumAmountColumn(lines));
    }

    static BigDecimal sumAmountColumn(List<String> lines) {
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("CSV is empty: a header row is required");
        }
        int amountIndex = amountColumnIndex(lines.get(0));
        BigDecimal total = BigDecimal.ZERO;
        for (String row : lines.subList(1, lines.size())) {
            if (row.isBlank()) {
                continue;
            }
            total = total.add(amountIn(row, amountIndex));
        }
        return total;
    }

    private static int amountColumnIndex(String headerRow) {
        String[] headers = headerRow.split(COLUMN_SEPARATOR, -1);
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].trim().equalsIgnoreCase(AMOUNT_COLUMN)) {
                return i;
            }
        }
        throw new IllegalArgumentException(
                "No '" + AMOUNT_COLUMN + "' column in header: " + headerRow);
    }

    private static BigDecimal amountIn(String row, int amountIndex) {
        String[] cells = row.split(COLUMN_SEPARATOR, -1);
        if (amountIndex >= cells.length) {
            throw new IllegalArgumentException("Row is missing the amount cell: " + row);
        }
        return new BigDecimal(cells[amountIndex].trim());
    }
}
