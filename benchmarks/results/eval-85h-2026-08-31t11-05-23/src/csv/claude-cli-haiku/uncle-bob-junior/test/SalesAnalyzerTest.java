import java.io.BufferedReader;
import java.io.StringReader;
import java.io.IOException;

public class SalesAnalyzerTest {
    public static void main(String[] args) throws IOException {
        testFindColumnIndex();
        testExtractAmount();
        testSumAmounts();
        System.out.println("All tests passed");
    }

    private static void testFindColumnIndex() {
        assert SalesAnalyzer.findColumnIndex("id,amount,date") == 1;
        assert SalesAnalyzer.findColumnIndex("id, amount , date") == 1;
        assert SalesAnalyzer.findColumnIndex("id,AMOUNT,date") == 1;
        assert SalesAnalyzer.findColumnIndex("id,price,date") == -1;
    }

    private static void testExtractAmount() {
        assert SalesAnalyzer.extractAmount("100,200.50,300", 1) == 200.50;
        assert SalesAnalyzer.extractAmount("100,invalid,300", 1) == 0.0;
        assert SalesAnalyzer.extractAmount("100,200", 5) == 0.0;
    }

    private static void testSumAmounts() throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader("10.5\n20.3\n15.2"));
        assert SalesAnalyzer.sumAmounts(reader, 0) == 46.0;
        
        reader = new BufferedReader(new StringReader("10.5\ninvalid\n20.3"));
        assert SalesAnalyzer.sumAmounts(reader, 0) == 30.8;
    }
}
