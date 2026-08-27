import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

/**
 * Runs an operation, retrying up to a maximum number of attempts when it
 * throws, with a fixed delay between attempts.
 */
public final class Retry {

    private final int maxAttempts;
    private final Duration delay;
    private final Predicate<Exception> retryOn;

    private Retry(int maxAttempts, Duration delay, Predicate<Exception> retryOn) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        if (delay.isNegative()) {
            throw new IllegalArgumentException("delay must not be negative");
        }
        this.maxAttempts = maxAttempts;
        this.delay = delay;
        this.retryOn = Objects.requireNonNull(retryOn);
    }

    /** Retries on any exception. */
    public static Retry of(int maxAttempts, Duration delay) {
        return new Retry(maxAttempts, delay, e -> true);
    }

    /** Retries only when the given predicate matches the thrown exception. */
    public static Retry of(int maxAttempts, Duration delay, Predicate<Exception> retryOn) {
        return new Retry(maxAttempts, delay, retryOn);
    }

    /**
     * Executes the operation, retrying on failure.
     *
     * @return the operation's result on the first successful attempt
     * @throws RetryExhaustedException if all attempts fail; the last exception
     *         is the cause, earlier ones are attached as suppressed
     * @throws InterruptedException if interrupted while waiting between attempts
     */
    public <T> T execute(Callable<T> operation) throws InterruptedException {
        Objects.requireNonNull(operation);
        Exception last = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (InterruptedException e) {
                throw e;
            } catch (Exception e) {
                if (last != null) {
                    e.addSuppressed(last);
                }
                last = e;
                if (attempt == maxAttempts || !retryOn.test(e)) {
                    break;
                }
                Thread.sleep(delay.toMillis());
            }
        }
        throw new RetryExhaustedException(
                "Operation failed after " + maxAttempts + " attempt(s)", last);
    }

    /** Convenience overload for operations that return nothing. */
    public void execute(RunnableWithException operation) throws InterruptedException {
        execute(() -> {
            operation.run();
            return null;
        });
    }

    @FunctionalInterface
    public interface RunnableWithException {
        void run() throws Exception;
    }

    /** Thrown when all retry attempts have been exhausted. */
    public static class RetryExhaustedException extends RuntimeException {
        public RetryExhaustedException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
