class AllTests {
    public static void main(String[] args) {
        ExchangeRatesTest.runAll();
        System.out.println("✓ ExchangeRatesTest passed");
        
        CategoryClassifierTest.runAll();
        System.out.println("✓ CategoryClassifierTest passed");
        
        TransactionParserTest.runAll();
        System.out.println("✓ TransactionParserTest passed");
        
        System.out.println("\nAll tests passed!");
    }
}
