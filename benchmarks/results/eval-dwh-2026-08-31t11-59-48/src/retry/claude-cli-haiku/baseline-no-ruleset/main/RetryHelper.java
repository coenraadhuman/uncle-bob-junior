import java.util.concurrent.Callable;

public class RetryHelper {
    @FunctionalInterface
    public interface CheckedRunnable {
        void run() throws Exception;
    }

    @FunctionalInterface
    public interface CheckedSupplier<T> {
        T get() throws Exception;
    }

    public static <T> T retry(CheckedSupplier<T> operation, int maxAttempts, long delayMillis) throws Exception {
        Exception lastException = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.get();
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts) {
                    Thread.sleep(delayMillis);
                }
            }
        }
        throw lastException;
    }

    public static void retry(CheckedRunnable operation, int maxAttempts, long delayMillis) throws Exception {
        Exception lastException = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                operation.run();
                return;
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts) {
                    Thread.sleep(delayMillis);
                }
            }
        }
        if (lastException != null) {
            throw lastException;
        }
    }
}
