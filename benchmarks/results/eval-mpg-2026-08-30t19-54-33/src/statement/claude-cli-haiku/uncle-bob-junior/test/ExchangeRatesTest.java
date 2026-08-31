class ExchangeRatesTest {
    static void runAll() {
        testConvertUsd();
        testConvertGbp();
        testConvertEur();
    }

    private static void testConvertUsd() {
        BigDecimal result = ExchangeRates.convertToEur(new BigDecimal("100"), "USD");
        assertEqual(new BigDecimal("92.00"), result);
    }

    private static void testConvertGbp() {
        BigDecimal result = ExchangeRates.convertToEur(new BigDecimal("100"), "GBP");
        assertEqual(new BigDecimal("117.00"), result);
    }

    private static void testConvertEur() {
        BigDecimal result = ExchangeRates.convertToEur(new BigDecimal("100"), "EUR");
        assertEqual(new BigDecimal("100.00"), result);
    }

    private static void assertEqual(BigDecimal expected, BigDecimal actual) {
        if (expected.compareTo(actual) != 0) {
            throw new AssertionError("Expected " + expected + " but got " + actual);
        }
    }
}
