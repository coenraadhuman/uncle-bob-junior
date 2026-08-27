import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class SalesSum {

    public static void main(String[] args) {
        Path file = Path.of(args.length > 0 ? args[0] : "sales.csv");

        List<String> lines;
        try {
            lines = Files.readAllLines(file);
        } catch (IOException e) {
            System.err.println("Could not read " + file + ": " + e.getMessage());
            System.exit(1);
            return;
        }

        if (lines.isEmpty()) {
            System.err.println("File is empty: " + file);
            System.exit(1);
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
            System.err.println("No 'amount' column found in header: " + lines.get(0));
            System.exit(1);
            return;
        }

        BigDecimal sum = BigDecimal.ZERO;
        for (int lineNo = 1; lineNo < lines.size(); lineNo++) {
            String line = lines.get(lineNo);
            if (line.isBlank()) {
                continue;
            }
            String[] fields = line.split(",", -1);
            if (amountIndex >= fields.length) {
                System.err.println("Skipping line " + (lineNo + 1) + ": too few columns");
                continue;
            }
            String value = fields[amountIndex].trim();
            try {
                sum = sum.add(new BigDecimal(value));
            } catch (NumberFormatException e) {
                System.err.println("Skipping line " + (lineNo + 1) + ": invalid amount '" + value + "'");
            }
        }

        System.out.println(sum.toPlainString());
    }
}
