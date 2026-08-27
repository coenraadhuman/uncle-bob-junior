// SalesTotalPrinter.java
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class SalesTotalPrinter {

    private static final Path SALES_FILE = Path.of("sales.csv");

    public static void main(String[] args) {
        try {
            List<String> lines = Files.readAllLines(SALES_FILE);
            BigDecimal total = new SalesAmountSummer().sum(lines);
            System.out.println(total);
        } catch (IOException e) {
            System.err.println("Could not read " + SALES_FILE + ": " + e.getMessage());
            System.exit(1);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid sales.csv: " + e.getMessage());
            System.exit(1);
        }
    }
}

final class SalesAmountSummer {

    private static final String CSV_DELIMITER = ",";
    private static final String AMOUNT_COLUMN_NAME = "amount";

    BigDecimal sum(List<String> lines) {
        if (lines.isEmpty()) {
            return BigDecimal.ZERO;
        }

        int amountColumnIndex = findAmountColumnIndex(lines.get(0));
        BigDecimal total = BigDecimal.ZERO;
        for (String line : lines.subList(1, lines.size())) {
            if (line.isBlank()) {
                continue;
            }
            total = total.add(extractAmount(line, amountColumnIndex));
        }
        return total;
    }

    private int findAmountColumnIndex(String headerLine) {
        String[] headers = headerLine.split(CSV_DELIMITER, -1);
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].trim().equalsIgnoreCase(AMOUNT_COLUMN_NAME)) {
                return i;
            }
        }
        throw new IllegalArgumentException("header is missing an 'amount' column");
    }

    private BigDecimal extractAmount(String dataLine, int amountColumnIndex) {
        String[] fields = dataLine.split(CSV_DELIMITER, -1);
        return new BigDecimal(fields[amountColumnIndex].trim());
    }
}
