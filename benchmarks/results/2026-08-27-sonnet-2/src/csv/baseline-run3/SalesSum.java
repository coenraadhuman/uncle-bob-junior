import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class SalesSum {

    public static void main(String[] args) {
        Path csvPath = Path.of("sales.csv");

        try {
            List<String> lines = Files.readAllLines(csvPath);

            if (lines.isEmpty()) {
                System.out.println("sales.csv is empty.");
                return;
            }

            String[] header = lines.get(0).split(",");
            int amountIndex = -1;
            for (int i = 0; i < header.length; i++) {
                if (header[i].trim().equalsIgnoreCase("amount")) {
                    amountIndex = i;
                    break;
                }
            }

            if (amountIndex == -1) {
                System.out.println("No 'amount' column found in sales.csv.");
                return;
            }

            BigDecimal total = BigDecimal.ZERO;
            int rowsParsed = 0;
            int rowsSkipped = 0;

            for (int lineNo = 1; lineNo < lines.size(); lineNo++) {
                String line = lines.get(lineNo);
                if (line.isBlank()) {
                    continue;
                }

                String[] fields = line.split(",");
                if (amountIndex >= fields.length) {
                    rowsSkipped++;
                    continue;
                }

                String rawValue = fields[amountIndex].trim();
                try {
                    total = total.add(new BigDecimal(rawValue));
                    rowsParsed++;
                } catch (NumberFormatException e) {
                    rowsSkipped++;
                    System.out.println("Skipping line " + (lineNo + 1) + ": invalid amount '" + rawValue + "'");
                }
            }

            total = total.setScale(2, RoundingMode.HALF_UP);
            System.out.println("Rows summed: " + rowsParsed + (rowsSkipped > 0 ? ", skipped: " + rowsSkipped : ""));
            System.out.println("Total amount: " + total);

        } catch (IOException e) {
            System.err.println("Could not read sales.csv: " + e.getMessage());
        }
    }
}
