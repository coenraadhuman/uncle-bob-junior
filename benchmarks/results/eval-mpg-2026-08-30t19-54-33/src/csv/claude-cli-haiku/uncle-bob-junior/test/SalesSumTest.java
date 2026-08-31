import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SalesSumTest {
    
    @Test
    void findColumnIndex_locatesAmountColumn() {
        int index = SalesSum.findColumnIndex("id,date,amount,product", "amount");
        assertEquals(2, index);
    }
    
    @Test
    void findColumnIndex_caseInsensitive() {
        int index = SalesSum.findColumnIndex("id,Amount,product", "amount");
        assertEquals(1, index);
    }
    
    @Test
    void findColumnIndex_notFound() {
        int index = SalesSum.findColumnIndex("id,date,product", "amount");
        assertEquals(-1, index);
    }
    
    @Test
    void extractAmount_parsesValidDouble() {
        double amount = SalesSum.extractAmount("1,2026-08-30,99.50,Widget", 2);
        assertEquals(99.50, amount);
    }
    
    @Test
    void extractAmount_handlesInvalidNumber() {
        double amount = SalesSum.extractAmount("1,2026-08-30,invalid,Widget", 2);
        assertEquals(0.0, amount);
    }
    
    @Test
    void extractAmount_columnIndexOutOfBounds() {
        double amount = SalesSum.extractAmount("1,2026-08-30", 5);
        assertEquals(0.0, amount);
    }
    
    @Test
    void sumAmountColumn_sumsAllAmounts(@TempDir Path dir) throws IOException {
        Path csvFile = dir.resolve("sales.csv");
        Files.writeString(csvFile, "id,amount,name\n1,10.0,A\n2,20.0,B\n3,30.0,C\n");
        
        double sum = SalesSum.sumAmountColumn(csvFile.toString());
        assertEquals(60.0, sum);
    }
    
    @Test
    void sumAmountColumn_emptyFile(@TempDir Path dir) throws IOException {
        Path csvFile = dir.resolve("sales.csv");
        Files.writeString(csvFile, "");
        
        double sum = SalesSum.sumAmountColumn(csvFile.toString());
        assertEquals(0.0, sum);
    }
    
    @Test
    void sumAmountColumn_missingAmountColumn(@TempDir Path dir) throws IOException {
        Path csvFile = dir.resolve("sales.csv");
        Files.writeString(csvFile, "id,name\n1,A\n");
        
        assertThrows(IllegalArgumentException.class, () -> 
            SalesSum.sumAmountColumn(csvFile.toString())
        );
    }
}
