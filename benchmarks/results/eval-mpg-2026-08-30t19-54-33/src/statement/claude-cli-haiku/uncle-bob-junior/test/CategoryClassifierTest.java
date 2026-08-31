class CategoryClassifierTest {
    static void runAll() {
        testSalary();
        testRent();
        testGroceries();
        testOther();
    }

    private static void testSalary() {
        assertEqual("salary", CategoryClassifier.categorize("Monthly salary"));
    }

    private static void testRent() {
        assertEqual("rent", CategoryClassifier.categorize("Rent payment"));
    }

    private static void testGroceries() {
        assertEqual("groceries", CategoryClassifier.categorize("ALBERT HEIJN"));
    }

    private static void testOther() {
        assertEqual("other", CategoryClassifier.categorize("Random"));
    }

    private static void assertEqual(String expected, String actual) {
        if (!expected.equals(actual)) {
            throw new AssertionError("Expected " + expected + " but got " + actual);
        }
    }
}
