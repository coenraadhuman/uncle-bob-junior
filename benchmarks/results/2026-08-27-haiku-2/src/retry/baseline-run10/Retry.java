import java.util.concurrent.Callable;

/**
 * Retry helper that executes an operation with exponential backoff.
 * Assumes RuntimeException or checked exceptions wrapped in RuntimeException.
 */
public class Retry {
    private final int maxAttempts;
    private final long delayMs;

    public Retry(int maxAttempts, long delayMs) {
        this.maxAttempts = maxAttempts;
        this.delayMs = delayMs;
    }

    public <T> T execute(Callable<T> operation) throws Exception {
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts) {
                    Thread.sleep(delayMs);
                }
            }
        }

        throw lastException;
    }

    public void executeVoid(VoidCallable operation) throws Exception {
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                operation.call();
                return;
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts) {
                    Thread.sleep(delayMs);
                }
            }
        }

        throw lastException;
    }

    @FunctionalInterface
    public interface VoidCallable {
        void call() throws Exception;
    }
}
