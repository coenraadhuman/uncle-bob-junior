import java.time.Duration;
import java.util.concurrent.Callable;

public final class RetryHelper {

    private RetryHelper() {
    }

    public static <T> T retry(Callable<T> operation, int maxAttempts, Duration delay) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }

        Exception lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (Exception e) {
                lastException = e;

                boolean isLastAttempt = attempt == maxAttempts;
                if (isLastAttempt) {
                    break;
                }

                try {
                    Thread.sleep(delay.toMillis());
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Retry interrupted while waiting before attempt " + (attempt + 1), interruptedException);
                }
            }
        }

        if (lastException instanceof RuntimeException) {
            throw (RuntimeException) lastException;
        }
        throw new RuntimeException("Operation failed after " + maxAttempts + " attempts", lastException);
    }

    // Convenience overload for operations that return no result
    public static void retry(RunnableWithException operation, int maxAttempts, Duration delay) {
        retry(() -> {
            operation.run();
            return null;
        }, maxAttempts, delay);
    }

    @FunctionalInterface
    public interface RunnableWithException {
        void run() throws Exception;
    }
}
