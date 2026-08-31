import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class SalesAmountSummer {

    private static final String COLUMN_SEPARATOR = ",";
    private static final String AMOUNT_COLUMN_NAME = "amount";

    public static void main(String[] args) throws IOException {
        Path csvPath = Path.of(args.length > 0 ? args[0] : "sales.csv");
        List<String> lines = Files.readAllLines(csvPath);
        double total = sumAmountColumn(lines);
        System.out.printf("Total amount: %.2f%n", total);
    }

    /**
     * Sums the "amount" column of CSV lines.
     *
     * @param csvLines lines including a header row that names the "amount" column
     * @return sum of all "amount" values in the data rows
     * @throws IllegalArgumentException if csvLines is empty or has no "amount" column
     */
    public static double sumAmountColumn(List<String> csvLines) {
        if (csvLines.isEmpty()) {
            throw new IllegalArgumentException("CSV has no header row");
        }
        int amountColumnIndex = findAmountColumnIndex(csvLines.get(0));
        double total = 0;
        for (String dataLine : csvLines.subList(1, csvLines.size())) {
            total += parseAmount(dataLine, amountColumnIndex);
        }
        return total;
    }

    private static int findAmountColumnIndex(String headerLine) {
        String[] headers = headerLine.split(COLUMN_SEPARATOR);
        for (int index = 0; index < headers.length; index++) {
            if (headers[index].trim().equalsIgnoreCase(AMOUNT_COLUMN_NAME)) {
                return index;
            }
        }
        throw new IllegalArgumentException("No '" + AMOUNT_COLUMN_NAME + "' column found");
    }

    private static double parseAmount(String dataLine, int amountColumnIndex) {
        String[] fields = dataLine.split(COLUMN_SEPARATOR);
        return Double.parseDouble(fields[amountColumnIndex].trim());
    }
}
