class TransactionParserTest {
    static void runAll() {
        testValidParse();
        testInvalidFormat();
        testInvalidDate();
    }

    private static void testValidParse() {
        Transaction t = TransactionParser.parse("2026-01-15;ALBERT HEIJN;-23.95;EUR");
        if (t == null || !t.getDescription().equals("ALBERT HEIJN")) {
            throw new AssertionError("Failed to parse valid transaction");
        }
    }

    private static void testInvalidFormat() {
        Transaction t = TransactionParser.parse("invalid");
        if (t != null) throw new AssertionError("Should return null for invalid format");
    }

    private static void testInvalidDate() {
        Transaction t = TransactionParser.parse("not-date;desc;100;EUR");
        if (t != null) throw new AssertionError("Should return null for invalid date");
    }
}
