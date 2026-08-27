import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class SalesTotal {

    private static final Path SALES_FILE = Path.of("sales.csv");
    private static final String AMOUNT_COLUMN = "amount";

    private SalesTotal() {
    }

    public static void main(String[] args) throws IOException {
        List<String> lines = Files.readAllLines(SALES_FILE);
        BigDecimal total = CsvAmountSummarizer.sumColumn(lines, AMOUNT_COLUMN);
        System.out.println(total);
    }
}
