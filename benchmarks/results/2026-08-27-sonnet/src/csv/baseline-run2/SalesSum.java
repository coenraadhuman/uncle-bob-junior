import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class SalesSum {

    public static void main(String[] args) throws IOException {
        Path csvPath = Path.of("sales.csv");
        List<String> lines = Files.readAllLines(csvPath);

        if (lines.isEmpty()) {
            System.out.println("sales.csv is empty; sum = 0");
            return;
        }

        String[] header = lines.get(0).split(",", -1);
        int amountIndex = -1;
        for (int i = 0; i < header.length; i++) {
            if (header[i].trim().equalsIgnoreCase("amount")) {
                amountIndex = i;
                break;
            }
        }

        if (amountIndex == -1) {
            System.out.println("No 'amount' column found in header.");
            return;
        }

        BigDecimal total = BigDecimal.ZERO;
        int skipped = 0;

        for (int lineNo = 1; lineNo < lines.size(); lineNo++) {
            String line = lines.get(lineNo);
            if (line.isBlank()) {
                continue;
            }
            String[] fields = line.split(",", -1);
            if (amountIndex >= fields.length) {
                skipped++;
                continue;
            }
            String rawValue = fields[amountIndex].trim();
            if (rawValue.isEmpty()) {
                continue;
            }
            try {
                total = total.add(new BigDecimal(rawValue));
            } catch (NumberFormatException e) {
                System.err.println("Skipping unparseable value on line " + (lineNo + 1) + ": " + rawValue);
                skipped++;
            }
        }

        System.out.println("Sum of 'amount': " + total);
        if (skipped > 0) {
            System.out.println("(" + skipped + " row(s) skipped due to missing/invalid values)");
        }
    }
}
