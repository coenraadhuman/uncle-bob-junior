import java.util.function.Supplier;

public class Example {
    public static void main(String[] args) throws Exception {
        // Retry with return value (3 attempts, 1 second delay)
        RetryHelper retrier = RetryHelper.withAttempts(3);
        
        String result = retrier.execute(() -> {
            System.out.println("Attempting operation...");
            if (Math.random() > 0.7) {
                return "Success!";
            }
            throw new RuntimeException("Operation failed");
        });
        System.out.println(result);

        // Retry void operation (5 attempts, 500ms delay)
        RetryHelper retrier2 = RetryHelper.withAttemptsAndDelay(5, 500);
        
        retrier2.execute(() -> {
            System.out.println("Performing action...");
            if (Math.random() > 0.6) {
                throw new IOException("Network error");
            }
        });
    }
}
