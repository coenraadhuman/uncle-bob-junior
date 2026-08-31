public class RetryHelperExample {
    public static void main(String[] args) {
        int result = RetryHelper.retry(() -> {
            // some operation that might throw
            return riskyOperation();
        }, 5, Duration.ofSeconds(2));

        RetryHelper.retry(() -> {
            // void operation that might throw
            riskyVoidOperation();
        }, 3, Duration.ofMillis(500));
    }

    private static int riskyOperation() throws Exception {
        return 42;
    }

    private static void riskyVoidOperation() throws Exception {
    }
}
