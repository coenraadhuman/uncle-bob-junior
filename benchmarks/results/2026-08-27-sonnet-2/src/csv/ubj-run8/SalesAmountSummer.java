import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class SalesAmountSummer {

    private static final String AMOUNT_COLUMN_NAME = "amount";
    private static final String CSV_DELIMITER = ",";

    private SalesAmountSummer() {
    }

    public static double sumAmountColumn(Path csvPath) throws IOException {
        List<String> lines = Files.readAllLines(csvPath);
        if (lines.isEmpty()) {
            return 0.0;
        }

        int amountColumnIndex = findAmountColumnIndex(lines.get(0));
        double total = 0.0;
        for (String line : lines.subList(1, lines.size())) {
            total += parseAmount(line, amountColumnIndex);
        }
        return total;
    }

    private static int findAmountColumnIndex(String headerLine) {
        String[] headers = headerLine.split(CSV_DELIMITER, -1);
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].trim().equalsIgnoreCase(AMOUNT_COLUMN_NAME)) {
                return i;
            }
        }
        throw new IllegalArgumentException("CSV has no '" + AMOUNT_COLUMN_NAME + "' column");
    }

    private static double parseAmount(String dataLine, int amountColumnIndex) {
        if (dataLine.isBlank()) {
            return 0.0;
        }
        String[] fields = dataLine.split(CSV_DELIMITER, -1);
        return Double.parseDouble(fields[amountColumnIndex].trim());
    }

    public static void main(String[] args) {
        Path csvPath = Path.of(args.length > 0 ? args[0] : "sales.csv");
        try {
            double sum = sumAmountColumn(csvPath);
            System.out.println("Sum of amount column: " + sum);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read " + csvPath, e);
        }
    }
}
