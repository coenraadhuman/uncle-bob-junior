// SalesAmountSummer.java
import java.io.BufferedReader;
import java.io.IOException;

public final class SalesAmountSummer {

    private static final String COLUMN_SEPARATOR = ",";
    private static final String AMOUNT_COLUMN_NAME = "amount";

    private SalesAmountSummer() {
    }

    public static double sumAmountColumn(BufferedReader csvReader) throws IOException {
        String headerLine = csvReader.readLine();
        if (headerLine == null) {
            return 0.0;
        }
        int amountColumnIndex = findAmountColumnIndex(headerLine);

        double total = 0.0;
        String row;
        while ((row = csvReader.readLine()) != null) {
            total += parseAmount(row, amountColumnIndex);
        }
        return total;
    }

    private static int findAmountColumnIndex(String headerLine) {
        String[] columnNames = headerLine.split(COLUMN_SEPARATOR);
        for (int i = 0; i < columnNames.length; i++) {
            if (columnNames[i].trim().equalsIgnoreCase(AMOUNT_COLUMN_NAME)) {
                return i;
            }
        }
        throw new IllegalArgumentException("CSV header has no '" + AMOUNT_COLUMN_NAME + "' column");
    }

    private static double parseAmount(String row, int amountColumnIndex) {
        String[] values = row.split(COLUMN_SEPARATOR);
        return Double.parseDouble(values[amountColumnIndex].trim());
    }
}
