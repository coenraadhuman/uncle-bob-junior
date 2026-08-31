import java.util.concurrent.Callable;

public class Retry {
    public static <T> T execute(Callable<T> operation, RetryConfig config) throws Exception {
        Exception lastException = null;
        for (int attempt = 1; attempt <= config.maxAttempts(); attempt++) {
            try {
                return operation.call();
            } catch (Exception e) {
                lastException = e;
                if (attempt < config.maxAttempts()) {
                    sleepWithInterruptHandling(config.delayMs());
                }
            }
        }
        throw lastException;
    }

    private static void sleepWithInterruptHandling(long delayMs) throws InterruptedException {
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw ie;
        }
    }
}
