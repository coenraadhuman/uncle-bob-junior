import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class SalesAmountSummer {

    private static final Path SALES_FILE = Path.of("sales.csv");
    private static final String AMOUNT_COLUMN = "amount";
    private static final String DELIMITER = ",";

    public static void main(String[] args) {
        try {
            List<String> lines = Files.readAllLines(SALES_FILE, StandardCharsets.UTF_8);
            System.out.println(sumAmountColumn(lines));
        } catch (IOException e) {
            System.err.println("Could not read " + SALES_FILE + ": " + e.getMessage());
            System.exit(1);
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    static BigDecimal sumAmountColumn(List<String> lines) {
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("sales.csv is empty: no header row found");
        }
        int amountIndex = findAmountColumnIndex(lines.get(0));

        BigDecimal total = BigDecimal.ZERO;
        for (int lineNumber = 2; lineNumber <= lines.size(); lineNumber++) {
            String line = lines.get(lineNumber - 1);
            if (line.isBlank()) {
                continue;
            }
            total = total.add(parseAmount(line, amountIndex, lineNumber));
        }
        return total;
    }

    private static int findAmountColumnIndex(String headerLine) {
        String[] headers = headerLine.split(DELIMITER, -1);
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].trim().equalsIgnoreCase(AMOUNT_COLUMN)) {
                return i;
            }
        }
        throw new IllegalArgumentException(
                "No '" + AMOUNT_COLUMN + "' column in header: " + headerLine);
    }

    private static BigDecimal parseAmount(String line, int amountIndex, int lineNumber) {
        String[] fields = line.split(DELIMITER, -1);
        if (amountIndex >= fields.length) {
            throw new IllegalArgumentException(
                    "Line " + lineNumber + " has no value in the '" + AMOUNT_COLUMN + "' column");
        }
        try {
            return new BigDecimal(fields[amountIndex].trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Line " + lineNumber + " has a non-numeric amount: " + fields[amountIndex]);
        }
    }

    private SalesAmountSummer() {
    }
}
