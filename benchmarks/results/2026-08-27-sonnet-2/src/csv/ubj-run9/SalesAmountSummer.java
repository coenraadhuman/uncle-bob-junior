import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SalesAmountSummer {

    private static final String DEFAULT_FILE_PATH = "sales.csv";
    private static final String COLUMN_SEPARATOR = ",";
    private static final String AMOUNT_COLUMN_NAME = "amount";

    public static void main(String[] args) throws IOException {
        String filePath = args.length > 0 ? args[0] : DEFAULT_FILE_PATH;
        double total = sumAmountColumn(Paths.get(filePath));
        System.out.printf("Total amount: %.2f%n", total);
    }

    static double sumAmountColumn(Path csvPath) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(csvPath)) {
            return sumAmountColumn(reader);
        }
    }

    static double sumAmountColumn(BufferedReader reader) throws IOException {
        String headerLine = reader.readLine();
        if (headerLine == null) {
            return 0.0;
        }

        int amountColumnIndex = findAmountColumnIndex(headerLine);
        double total = 0.0;
        String line;
        while ((line = reader.readLine()) != null) {
            total += parseAmount(line, amountColumnIndex);
        }
        return total;
    }

    private static int findAmountColumnIndex(String headerLine) {
        String[] columns = headerLine.split(COLUMN_SEPARATOR);
        for (int i = 0; i < columns.length; i++) {
            if (columns[i].trim().equalsIgnoreCase(AMOUNT_COLUMN_NAME)) {
                return i;
            }
        }
        throw new IllegalArgumentException(
            "CSV header does not contain an '" + AMOUNT_COLUMN_NAME + "' column");
    }

    private static double parseAmount(String line, int amountColumnIndex) {
        if (line.isBlank()) {
            return 0.0;
        }
        String[] fields = line.split(COLUMN_SEPARATOR);
        return Double.parseDouble(fields[amountColumnIndex].trim());
    }
}
