import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Reads sales.csv from the working directory and prints the sum of its
 * 'amount' column. Expects a header row; fields must not contain embedded
 * commas or quotes.
 */
public final class SalesAmountSummer {

    private static final Path SALES_FILE = Path.of("sales.csv");
    private static final String AMOUNT_COLUMN = "amount";
    private static final String FIELD_SEPARATOR = ",";

    public static void main(String[] args) throws IOException {
        List<String> lines = Files.readAllLines(SALES_FILE);
        if (lines.isEmpty()) {
            System.err.println("sales.csv is empty");
            System.exit(1);
        }

        int amountColumnIndex = findAmountColumnIndex(lines.get(0));
        BigDecimal total = sumAmounts(lines.subList(1, lines.size()), amountColumnIndex);
        System.out.println(total);
    }

    private static int findAmountColumnIndex(String headerLine) {
        String[] headers = headerLine.split(FIELD_SEPARATOR);
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].trim().equalsIgnoreCase(AMOUNT_COLUMN)) {
                return i;
            }
        }
        throw new IllegalArgumentException("No '" + AMOUNT_COLUMN + "' column in header: " + headerLine);
    }

    private static BigDecimal sumAmounts(List<String> dataLines, int amountColumnIndex) {
        BigDecimal total = BigDecimal.ZERO;
        for (String line : dataLines) {
            if (line.isBlank()) {
                continue;
            }
            total = total.add(parseAmount(line, amountColumnIndex));
        }
        return total;
    }

    private static BigDecimal parseAmount(String line, int amountColumnIndex) {
        String[] fields = line.split(FIELD_SEPARATOR, -1);
        if (amountColumnIndex >= fields.length) {
            throw new IllegalArgumentException("Row has no amount field: " + line);
        }
        return new BigDecimal(fields[amountColumnIndex].trim());
    }
}
