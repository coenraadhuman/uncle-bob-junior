import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class RetryHelper {
    private final int maxAttempts;
    private final long delayMs;

    public RetryHelper(int maxAttempts, long delayMs) {
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

    public void executeVoid(VoidOperation operation) throws Exception {
        Exception lastException = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                operation.execute();
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
    public interface VoidOperation {
        void execute() throws Exception;
    }
}
