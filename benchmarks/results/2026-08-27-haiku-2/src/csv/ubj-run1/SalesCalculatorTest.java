import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SalesCalculatorTest {
    static void createTestCsv(String filePath, String content) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(content);
        }
    }

    static void test(String testName, double expected, double actual) {
        if (Math.abs(expected - actual) < 0.001) {
            System.out.println("✓ " + testName);
        } else {
            System.out.println("✗ " + testName + ": expected " + expected + ", got " + actual);
        }
    }

    public static void main(String[] args) throws IOException {
        // Test basic sum
        createTestCsv("test1.csv", "id,product,amount\n1,Widget,100.00\n2,Gadget,250.50\n");
        double result1 = SalesCalculator.sumAmountColumn("test1.csv");
        test("Basic sum", 350.50, result1);

        // Test single row
        createTestCsv("test2.csv", "id,product,amount\n1,Item,500\n");
        double result2 = SalesCalculator.sumAmountColumn("test2.csv");
        test("Single row", 500, result2);

        // Test with spaces around values
        createTestCsv("test3.csv", "id,product,amount\n1,A, 10.5 \n2,B, 20.5 \n");
        double result3 = SalesCalculator.sumAmountColumn("test3.csv");
        test("Values with spaces", 31.0, result3);

        // Cleanup
        Files.deleteIfExists(Paths.get("test1.csv"));
        Files.deleteIfExists(Paths.get("test2.csv"));
        Files.deleteIfExists(Paths.get("test3.csv"));
    }
}
