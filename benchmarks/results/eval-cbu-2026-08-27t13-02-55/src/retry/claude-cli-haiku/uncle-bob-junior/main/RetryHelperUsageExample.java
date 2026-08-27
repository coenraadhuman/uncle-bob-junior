public class RetryHelperUsageExample {
    public static void main(String[] args) {
        RetryHelper retryHelper = new RetryHelper(3, 1000);

        // Example 1: Operation that returns a value
        String result = retryHelper.execute(() -> {
            System.out.println("Attempting API call...");
            return callExternalApi();
        });

        // Example 2: Operation with side effects (no return value)
        retryHelper.execute(() -> {
            System.out.println("Attempting database write...");
            writeToDatabase();
            return null;
        });
    }

    private static String callExternalApi() throws IOException {
        // Simulated API call
        return "success";
    }

    private static void writeToDatabase() throws SQLException {
        // Simulated database operation
    }
}
