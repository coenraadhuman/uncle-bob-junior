import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class SalesAmountSummary {

    private static final String AMOUNT_COLUMN_NAME = "amount";
    private static final String CSV_DELIMITER = ",";

    public static void main(String[] args) throws IOException {
        Path csvPath = Path.of(args.length > 0 ? args[0] : "sales.csv");
        List<String> lines = Files.readAllLines(csvPath);
        double total = sumAmountColumn(lines);
        System.out.println(total);
    }

    static double sumAmountColumn(List<String> lines) {
        if (lines.isEmpty()) {
            return 0.0;
        }
        int amountColumnIndex = findAmountColumnIndex(lines.get(0));
        return lines.stream()
                .skip(1)
                .mapToDouble(line -> parseAmount(line, amountColumnIndex))
                .sum();
    }

    private static int findAmountColumnIndex(String headerLine) {
        String[] headers = headerLine.split(CSV_DELIMITER);
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].trim().equalsIgnoreCase(AMOUNT_COLUMN_NAME)) {
                return i;
            }
        }
        throw new IllegalArgumentException("CSV is missing an '" + AMOUNT_COLUMN_NAME + "' column");
    }

    private static double parseAmount(String line, int amountColumnIndex) {
        String[] fields = line.split(CSV_DELIMITER);
        return Double.parseDouble(fields[amountColumnIndex].trim());
    }
}
