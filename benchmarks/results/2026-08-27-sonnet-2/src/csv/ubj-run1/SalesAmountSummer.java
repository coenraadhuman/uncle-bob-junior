import java.util.List;

public final class SalesAmountSummer {

    private static final String COLUMN_NAME = "amount";
    private static final String DELIMITER = ",";

    private SalesAmountSummer() {
    }

    public static double sumAmounts(List<String> csvLines) {
        if (csvLines.isEmpty()) {
            return 0.0;
        }
        String[] header = csvLines.get(0).split(DELIMITER);
        int amountIndex = findColumnIndex(header, COLUMN_NAME);

        double total = 0.0;
        for (int i = 1; i < csvLines.size(); i++) {
            total += parseAmount(csvLines.get(i), amountIndex);
        }
        return total;
    }

    private static int findColumnIndex(String[] header, String columnName) {
        for (int i = 0; i < header.length; i++) {
            if (header[i].trim().equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        throw new IllegalArgumentException("CSV is missing required column: " + columnName);
    }

    private static double parseAmount(String line, int amountIndex) {
        String[] fields = line.split(DELIMITER);
        return Double.parseDouble(fields[amountIndex].trim());
    }
}
