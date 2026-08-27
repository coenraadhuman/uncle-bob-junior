import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SalesSum {

    public static void main(String[] args) {
        Path file = Path.of(args.length > 0 ? args[0] : "sales.csv");

        try {
            List<String> lines = Files.readAllLines(file);
            if (lines.isEmpty()) {
                System.err.println("Error: sales.csv is empty.");
                System.exit(1);
            }

            List<String> header = parseCsvLine(lines.get(0));
            int amountIndex = -1;
            for (int i = 0; i < header.size(); i++) {
                if (header.get(i).trim().equalsIgnoreCase("amount")) {
                    amountIndex = i;
                    break;
                }
            }
            if (amountIndex < 0) {
                System.err.println("Error: no 'amount' column found in header: " + header);
                System.exit(1);
            }

            BigDecimal sum = BigDecimal.ZERO;
            for (int lineNo = 1; lineNo < lines.size(); lineNo++) {
                String line = lines.get(lineNo);
                if (line.isBlank()) {
                    continue;
                }
                List<String> fields = parseCsvLine(line);
                if (fields.size() <= amountIndex) {
                    System.err.println("Warning: skipping line " + (lineNo + 1) + " (too few fields)");
                    continue;
                }
                String raw = fields.get(amountIndex).trim();
                if (raw.isEmpty()) {
                    continue;
                }
                try {
                    sum = sum.add(new BigDecimal(raw));
                } catch (NumberFormatException e) {
                    System.err.println("Warning: skipping line " + (lineNo + 1)
                            + " (invalid amount: '" + raw + "')");
                }
            }

            System.out.println(sum.toPlainString());
        } catch (IOException e) {
            System.err.println("Error reading " + file + ": " + e.getMessage());
            System.exit(1);
        }
    }

    // Minimal CSV parser: handles commas inside double-quoted fields and "" escapes.
    private static List<String> parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        current.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    current.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                } else if (c == ',') {
                    fields.add(current.toString());
                    current.setLength(0);
                } else {
                    current.append(c);
                }
            }
        }
        fields.add(current.toString());
        return fields;
    }
}
