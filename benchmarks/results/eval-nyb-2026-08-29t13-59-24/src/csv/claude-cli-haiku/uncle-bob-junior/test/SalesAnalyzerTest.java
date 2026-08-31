public class SalesAnalyzerTest {
    public static void main(String[] args) {
        testFindColumnIndex();
        System.out.println("All tests passed!");
    }

    private static void testFindColumnIndex() {
        assert SalesAnalyzer.findColumnIndex("id,name,amount", "amount") == 2;
        assert SalesAnalyzer.findColumnIndex("id,name,amount", "AMOUNT") == 2;
        assert SalesAnalyzer.findColumnIndex("id,name,total", "amount") == -1;
        assert SalesAnalyzer.findColumnIndex("amount,id,name", "amount") == 0;
    }
}
