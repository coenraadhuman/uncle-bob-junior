import java.util.List;

public final class SalesAmountSummer {
    private static final String CSV_DELIMITER = ",";
    private static final String AMOUNT_COLUMN_NAME = "amount";

    public double sumAmountColumn(List<String> csvLines) {
        if (csvLines.isEmpty()) {
            return 0.0;
        }
        int amountColumnIndex = findAmountColumnIndex(csvLines.get(0));
        double total = 0.0;
        for (String line : csvLines.subList(1, csvLines.size())) {
            total += parseAmount(line, amountColumnIndex);
        }
        return total;
    }

    private int findAmountColumnIndex(String headerLine) {
        String[] headers = headerLine.split(CSV_DELIMITER);
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].trim().equalsIgnoreCase(AMOUNT_COLUMN_NAME)) {
                return i;
            }
        }
        throw new IllegalArgumentException(
                "CSV header is missing an '" + AMOUNT_COLUMN_NAME + "' column");
    }

    private double parseAmount(String dataLine, int amountColumnIndex) {
        String[] fields = dataLine.split(CSV_DELIMITER);
        return Double.parseDouble(fields[amountColumnIndex].trim());
    }
}
