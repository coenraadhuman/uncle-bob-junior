import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

public class SalesAnalyzerTest {
    @Test
    void sumsSalesCorrectly(@TempDir Path tempDir) throws IOException {
        Path csvFile = tempDir.resolve("sales.csv");
        Files.write(csvFile, "amount,date\n10.5,2026-01-01\n20.3,2026-01-02\n15.2,2026-01-03\n".getBytes());
        
        double sum = SalesAnalyzer.calculateSalesSum(csvFile);
        
        assertEquals(46.0, sum, 0.01);
    }
    
    @Test
    void handlesEmptyFile(@TempDir Path tempDir) throws IOException {
        Path csvFile = tempDir.resolve("sales.csv");
        Files.write(csvFile, "amount\n".getBytes());
        
        double sum = SalesAnalyzer.calculateSalesSum(csvFile);
        
        assertEquals(0.0, sum);
    }
    
    @Test
    void skipsInvalidAmounts(@TempDir Path tempDir) throws IOException {
        Path csvFile = tempDir.resolve("sales.csv");
        Files.write(csvFile, "amount\n10\ninvalid\n20\n".getBytes());
        
        double sum = SalesAnalyzer.calculateSalesSum(csvFile);
        
        assertEquals(30.0, sum);
    }
    
    @Test
    void throwsWhenColumnMissing(@TempDir Path tempDir) throws IOException {
        Path csvFile = tempDir.resolve("sales.csv");
        Files.write(csvFile, "price,date\n10,2026-01-01\n".getBytes());
        
        assertThrows(IllegalArgumentException.class, () -> SalesAnalyzer.calculateSalesSum(csvFile));
    }
}
