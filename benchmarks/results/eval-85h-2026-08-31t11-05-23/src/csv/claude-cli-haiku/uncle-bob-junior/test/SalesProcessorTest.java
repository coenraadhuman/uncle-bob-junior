import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SalesProcessorTest {
    @Test
    void sumAmountsWithValidData() {
        List<String> lines = Arrays.asList(
            "product,amount",
            "Item1,10.5",
            "Item2,20.3",
            "Item3,15.2"
        );
        
        double result = SalesProcessor.sumAmounts(lines);
        
        assertEquals(46.0, result, 0.01);
    }
    
    @Test
    void sumAmountsWithEmptyLines() {
        List<String> lines = Arrays.asList("product,amount");
        
        double result = SalesProcessor.sumAmounts(lines);
        
        assertEquals(0.0, result);
    }
    
    @Test
    void sumAmountsWithAmountInDifferentPosition() {
        List<String> lines = Arrays.asList(
            "id,product,amount,date",
            "1,Item1,10.5,2026-01-01",
            "2,Item2,20.3,2026-01-02"
        );
        
        double result = SalesProcessor.sumAmounts(lines);
        
        assertEquals(30.8, result, 0.01);
    }
}
