import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.io.BufferedReader;
import java.io.StringReader;
import java.io.IOException;

public class SalesCalculatorTest {
    
    @Test
    void sumsSalesAmountsCorrectly() throws IOException {
        String csv = "name,amount\nProduct A,100.50\nProduct B,49.25\n";
        double sum = SalesCalculator.sumSalesFromReader(
            new BufferedReader(new StringReader(csv)));
        assertEquals(149.75, sum, 0.01);
    }
    
    @Test
    void handlesMultipleEntries() throws IOException {
        String csv = "id,name,amount\n1,Item,10\n2,Item,20\n3,Item,30\n";
        double sum = SalesCalculator.sumSalesFromReader(
            new BufferedReader(new StringReader(csv)));
        assertEquals(60, sum);
    }
    
    @Test
    void ignoresBlankLines() throws IOException {
        String csv = "name,amount\nProduct,100\n\nProduct,50\n";
        double sum = SalesCalculator.sumSalesFromReader(
            new BufferedReader(new StringReader(csv)));
        assertEquals(150, sum);
    }
    
    @Test
    void handlesColumnNameCaseInsensitive() throws IOException {
        String csv = "name,AMOUNT\nProduct,100\n";
        double sum = SalesCalculator.sumSalesFromReader(
            new BufferedReader(new StringReader(csv)));
        assertEquals(100, sum);
    }
    
    @Test
    void throwsWhenAmountColumnMissing() {
        String csv = "name,price\nProduct,100\n";
        assertThrows(IllegalArgumentException.class, 
            () -> SalesCalculator.sumSalesFromReader(
                new BufferedReader(new StringReader(csv))));
    }
    
    @Test
    void throwsOnInvalidAmount() {
        String csv = "name,amount\nProduct,invalid\n";
        assertThrows(IllegalArgumentException.class,
            () -> SalesCalculator.sumSalesFromReader(
                new BufferedReader(new StringReader(csv))));
    }
    
    @Test
    void throwsOnEmptyFile() {
        String csv = "";
        assertThrows(IllegalArgumentException.class,
            () -> SalesCalculator.sumSalesFromReader(
                new BufferedReader(new StringReader(csv))));
    }
}
