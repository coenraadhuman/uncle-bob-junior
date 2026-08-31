import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class SalesAnalyzerTest {
    public static void main(String[] args) throws IOException {
        testSumBasicCsv();
        testSumWithInvalidValues();
        testMissingAmountColumn();
        testEmptyFile();
        System.out.println("All tests passed");
    }
    
    private static void testSumBasicCsv() throws IOException {
        Path testFile = Files.createTempFile("sales", ".csv");
        Files.write(testFile, "id,amount,date\n1,100.50,2026-01-01\n2,200.75,2026-01-02".getBytes());
        double result = SalesAnalyzer.calculateSumFromCsv(testFile.toString());
        assert result == 301.25 : "Expected 301.25, got " + result;
        Files.delete(testFile);
    }
    
    private static void testSumWithInvalidValues() throws IOException {
        Path testFile = Files.createTempFile("sales", ".csv");
        Files.write(testFile, "id,amount\n1,50\n2,invalid\n3,75".getBytes());
        double result = SalesAnalyzer.calculateSumFromCsv(testFile.toString());
        assert result == 125 : "Expected 125, got " + result;
        Files.delete(testFile);
    }
    
    private static void testMissingAmountColumn() throws IOException {
        Path testFile = Files.createTempFile("sales", ".csv");
        Files.write(testFile, "id,price\n1,100".getBytes());
        try {
            SalesAnalyzer.calculateSumFromCsv(testFile.toString());
            assert false : "Should throw exception";
        } catch (IllegalArgumentException expected) {}
        Files.delete(testFile);
    }
    
    private static void testEmptyFile() throws IOException {
        Path testFile = Files.createTempFile("sales", ".csv");
        Files.write(testFile, "".getBytes());
        double result = SalesAnalyzer.calculateSumFromCsv(testFile.toString());
        assert result == 0 : "Expected 0, got " + result;
        Files.delete(testFile);
    }
}
