import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class SalesTotal {

    private static final Path SALES_CSV_PATH = Path.of("sales.csv");

    public static void main(String[] args) throws IOException {
        List<String> lines = Files.readAllLines(SALES_CSV_PATH);
        double total = SalesAmountSummer.sumAmounts(lines);
        System.out.printf("Total amount: %.2f%n", total);
    }
}
