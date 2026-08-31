import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class SalesAnalyzerTest {
    public static void main(String[] args) throws IOException {
        testSumWithValidCsv();
        testSumWithEmptyFile();
        testSumWithMissingAmountColumn();
        testSumWithInvalidNumbers();
        System.out.println("All tests passed!");
    }

    private static void testSumWithValidCsv() throws IOException {
        Path tempFile = Files.createTempFile("test_sales_", ".csv");
        try {
            String csv = "id,product,amount\n1,Widget,100.50\n2,Gadget,200.75\n3,Tool,50.25";
            Files.writeString(tempFile, csv);
            double sum = SalesAnalyzer.sumAmountColumn(tempFile.toString());
            assert sum == 351.50 : "Expected 351.50, got " + sum;
        } finally {
            Files.delete(tempFile);
        }
    }

    private static void testSumWithEmptyFile() throws IOException {
        Path tempFile = Files.createTempFile("test_sales_", ".csv");
        try {
            Files.writeString(tempFile, "");
            double sum = SalesAnalyzer.sumAmountColumn(tempFile.toString());
            assert sum == 0.0 : "Expected 0.0 for empty file";
        } finally {
            Files.delete(tempFile);
        }
    }

    private static void testSumWithMissingAmountColumn() throws IOException {
        Path tempFile = Files.createTempFile("test_sales_", ".csv");
        try {
            String csv = "id,product\n1,Widget";
            Files.writeString(tempFile, csv);
            try {
                SalesAnalyzer.sumAmountColumn(tempFile.toString());
                assert false : "Should throw IllegalArgumentException";
            } catch (IllegalArgumentException e) {
                assert e.getMessage().contains("'amount' column not found");
            }
        } finally {
            Files.delete(tempFile);
        }
    }

    private static void testSumWithInvalidNumbers() throws IOException {
        Path tempFile = Files.createTempFile("test_sales_", ".csv");
        try {
            String csv = "id,product,amount\n1,Widget,100.50\n2,Gadget,invalid\n3,Tool,50.25";
            Files.writeString(tempFile, csv);
            double sum = SalesAnalyzer.sumAmountColumn(tempFile.toString());
            assert sum == 150.75 : "Expected 150.75 (skipping invalid)";
        } finally {
            Files.delete(tempFile);
        }
    }
}
