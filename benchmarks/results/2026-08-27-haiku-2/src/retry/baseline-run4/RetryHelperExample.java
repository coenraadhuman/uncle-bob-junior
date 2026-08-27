public class RetryHelperExample {

    // Example 1: Retrying a method that returns a value
    public static void exampleWithReturnValue() throws Exception {
        String result = RetryHelper.retry(
            () -> callUnstableAPI(),
            3,           // max 3 attempts
            1000         // 1 second delay between retries
        );
        System.out.println("Success: " + result);
    }

    // Example 2: Retrying a void operation
    public static void exampleVoidOperation() throws Exception {
        RetryHelper.retry(
            () -> sendMessage("Hello"),
            5,           // max 5 attempts
            500          // 500ms delay
        );
        System.out.println("Message sent successfully");
    }

    // Example 3: With lambda that throws checked exception
    public static void exampleWithIOOperation() throws Exception {
        byte[] data = RetryHelper.retry(
            () -> downloadFile("https://example.com/file.txt"),
            3,
            2000
        );
    }

    private static String callUnstableAPI() throws Exception {
        if (Math.random() > 0.7) {
            return "Success!";
        }
        throw new Exception("API temporarily unavailable");
    }

    private static void sendMessage(String msg) throws Exception {
        if (Math.random() > 0.6) {
            return;
        }
        throw new Exception("Network error");
    }

    private static byte[] downloadFile(String url) throws Exception {
        throw new Exception("Connection timeout");
    }
}
