import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

public class SalesCalculatorTest {
    
    @Test
    void calculatesCorrectSum(@TempDir Path tempDir) throws IOException {
        Path csvFile = tempDir.resolve("sales.csv");
        Files.write(csvFile, "product,amount\nWidget,100.50\nGadget,200.25\n".getBytes());
        
        double result = SalesCalculator.calculateTotalSales(csvFile.toString());
        
        assertEquals(300.75, result, 0.01);
    }
    
    @Test
    void handlesEmptyFile(@TempDir Path tempDir) throws IOException {
        Path csvFile = tempDir.resolve("sales.csv");
        Files.write(csvFile, "amount\n".getBytes());
        
        double result = SalesCalculator.calculateTotalSales(csvFile.toString());
        
        assertEquals(0.0, result);
    }
    
    @Test
    void throwsWhenAmountColumnMissing(@TempDir Path tempDir) throws IOException {
        Path csvFile = tempDir.resolve("sales.csv");
        Files.write(csvFile, "product,price\nWidget,100\n".getBytes());
        
        assertThrows(IllegalArgumentException.class, 
            () -> SalesCalculator.calculateTotalSales(csvFile.toString()));
    }
    
    @Test
    void skipsInvalidNumbers(@TempDir Path tempDir) throws IOException {
        Path csvFile = tempDir.resolve("sales.csv");
        Files.write(csvFile, "amount\n100.50\ninvalid\n200.25\n".getBytes());
        
        double result = SalesCalculator.calculateTotalSales(csvFile.toString());
        
        assertEquals(300.75, result, 0.01);
    }
}
