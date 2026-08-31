import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class SalesAnalyzerTest {
    static void testSumWithValidCsv() throws IOException {
        List<String> csvLines = List.of(
            "id,product,amount",
            "1,Widget,10.50",
            "2,Gadget,20.25",
            "3,Doohickey,15.00"
        );

        Path tempFile = Files.createTempFile("test_sales", ".csv");
        Files.write(tempFile, csvLines);

        double result = SalesAnalyzer.calculateSalesTotal(tempFile.toString());
        assert result == 45.75 : "Expected 45.75, got " + result;

        Files.delete(tempFile);
        System.out.println("✓ testSumWithValidCsv");
    }

    static void testSumWithEmptyFile() throws IOException {
        Path tempFile = Files.createTempFile("test_sales", ".csv");
        Files.write(tempFile, List.of("id,product,amount"));

        double result = SalesAnalyzer.calculateSalesTotal(tempFile.toString());
        assert result == 0.0 : "Expected 0.0, got " + result;

        Files.delete(tempFile);
        System.out.println("✓ testSumWithEmptyFile");
    }

    static void testMissingColumnThrows() throws IOException {
        List<String> csvLines = List.of(
            "id,product,price",
            "1,Widget,10.50"
        );

        Path tempFile = Files.createTempFile("test_sales", ".csv");
        Files.write(tempFile, csvLines);

        try {
            SalesAnalyzer.calculateSalesTotal(tempFile.toString());
            assert false : "Expected IllegalArgumentException";
        } catch (IllegalArgumentException e) {
            System.out.println("✓ testMissingColumnThrows");
        }

        Files.delete(tempFile);
    }

    public static void main(String[] args) throws IOException {
        testSumWithValidCsv();
        testSumWithEmptyFile();
        testMissingColumnThrows();
        System.out.println("\nAll tests passed!");
    }
}
