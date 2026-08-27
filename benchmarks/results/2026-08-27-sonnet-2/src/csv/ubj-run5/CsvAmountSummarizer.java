import java.math.BigDecimal;
import java.util.List;

final class CsvAmountSummarizer {

    private static final String DELIMITER = ",";

    private CsvAmountSummarizer() {
    }

    static BigDecimal sumColumn(List<String> lines, String columnName) {
        if (lines.isEmpty()) {
            return BigDecimal.ZERO;
        }
        int columnIndex = findColumnIndex(lines.get(0), columnName);
        return lines.stream()
                .skip(1)
                .filter(line -> !line.isBlank())
                .map(line -> readAmount(line, columnIndex))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static int findColumnIndex(String headerLine, String columnName) {
        String[] header = headerLine.split(DELIMITER, -1);
        for (int i = 0; i < header.length; i++) {
            if (header[i].trim().equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Column not found: " + columnName);
    }

    private static BigDecimal readAmount(String line, int columnIndex) {
        String[] fields = line.split(DELIMITER, -1);
        return new BigDecimal(fields[columnIndex].trim());
    }
}
