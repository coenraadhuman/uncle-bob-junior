import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SalesCalculatorTest {
    public static void main(String[] args) throws IOException {
        testHappyPath();
        testEmptyFile();
        testMissingColumn();
        testSkipsInvalidAmounts();
        System.out.println("All tests passed");
    }
    
    private static void testHappyPath() throws IOException {
        String filename = "test_sales.csv";
        Files.write(Paths.get(filename), "id,amount,date\n1,100.50\n2,200.75\n3,150.25\n".getBytes());
        
        double total = new SalesCalculator().calculateTotal(filename);
        assert Math.abs(total - 451.50) < 0.01 : "Expected 451.50, got " + total;
        new File(filename).delete();
    }
    
    private static void testEmptyFile() throws IOException {
        String filename = "test_empty.csv";
        Files.write(Paths.get(filename), "amount\n".getBytes());
        
        double total = new SalesCalculator().calculateTotal(filename);
        assert total == 0.0 : "Expected 0.0 for file with only header";
        new File(filename).delete();
    }
    
    private static void testMissingColumn() throws IOException {
        String filename = "test_no_col.csv";
        Files.write(Paths.get(filename), "id,name\n1,Test\n".getBytes());
        
        try {
            new SalesCalculator().calculateTotal(filename);
            assert false : "Should throw IllegalArgumentException";
        } catch (IllegalArgumentException e) {
            assert e.getMessage().contains("amount");
        }
        new File(filename).delete();
    }
    
    private static void testSkipsInvalidAmounts() throws IOException {
        String filename = "test_invalid.csv";
        Files.write(Paths.get(filename), "id,amount\n1,100.50\n2,invalid\n3,75.25\n".getBytes());
        
        double total = new SalesCalculator().calculateTotal(filename);
        assert Math.abs(total - 175.75) < 0.01 : "Expected 175.75 (skipped invalid), got " + total;
        new File(filename).delete();
    }
}
