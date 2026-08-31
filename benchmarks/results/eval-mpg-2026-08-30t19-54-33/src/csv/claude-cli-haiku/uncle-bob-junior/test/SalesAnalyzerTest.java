import org.junit.jupiter.api.Test;
import java.io.BufferedReader;
import java.io.StringReader;
import static org.junit.jupiter.api.Assertions.*;

class SalesAnalyzerTest {
    @Test
    void sums_amounts_from_csv() throws Exception {
        String csv = "product,amount,date\n" +
                     "Widget,100.50,2026-01-01\n" +
                     "Gadget,250.75,2026-01-02\n" +
                     "Thing,49.99,2026-01-03";
        BufferedReader reader = new BufferedReader(new StringReader(csv));
        
        double total = new SalesAnalyzer().sumFromReader(reader);
        
        assertEquals(401.24, total, 0.01);
    }
    
    @Test
    void throws_when_amount_column_missing() {
        String csv = "product,price\n100,50";
        assertThrows(IllegalArgumentException.class, () -> 
            new SalesAnalyzer().sumFromReader(new BufferedReader(new StringReader(csv)))
        );
    }
    
    @Test
    void throws_when_header_missing() {
        assertThrows(IllegalArgumentException.class, () -> 
            new SalesAnalyzer().sumFromReader(new BufferedReader(new StringReader("")))
        );
    }
}
