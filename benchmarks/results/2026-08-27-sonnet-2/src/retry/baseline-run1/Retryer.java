import java.time.Duration;
import java.util.Objects;

/**
 * Reusable retry helper: runs an operation and retries it a fixed number of
 * times with a fixed delay between attempts when it throws an exception.
 */
public final class Retryer {

    private final int maxAttempts;
    private final Duration delay;

    private Retryer(int maxAttempts, Duration delay) {
        this.maxAttempts = maxAttempts;
        this.delay = delay;
    }

    /**
     * @param maxAttempts total number of attempts (including the first), must be >= 1
     * @param delay       fixed wait time between attempts
     */
    public static Retryer of(int maxAttempts, Duration delay) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be >= 1, was " + maxAttempts);
        }
        Objects.requireNonNull(delay, "delay");
        return new Retryer(maxAttempts, delay);
    }

    /** Operation that returns a value and may throw a checked exception. */
    @FunctionalInterface
    public interface RetryableCallable<T> {
        T call() throws Exception;
    }

    /** Operation that returns nothing and may throw a checked exception. */
    @FunctionalInterface
    public interface RetryableRunnable {
        void run() throws Exception;
    }

    /** Runs {@code operation}, retrying on any exception, and returns its result. */
    public <T> T call(RetryableCallable<T> operation) throws Exception {
        Exception lastFailure = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (Exception e) {
                if (lastFailure != null) {
                    e.addSuppressed(lastFailure);
                }
                lastFailure = e;

                boolean isLastAttempt = attempt == maxAttempts;
                if (isLastAttempt) {
                    throw e;
                }
                sleep(lastFailure);
            }
        }

        // Unreachable: loop always returns or throws.
        throw new AssertionError("Retry loop exited without result or exception");
    }

    /** Runs {@code operation}, retrying on any exception. */
    public void run(RetryableRunnable operation) throws Exception {
        call(() -> {
            operation.run();
            return null;
        });
    }

    private void sleep(Exception cause) throws InterruptedException {
        try {
            Thread.sleep(delay.toMillis());
        } catch (InterruptedException interrupted) {
            interrupted.addSuppressed(cause);
            Thread.currentThread().interrupt();
            throw interrupted;
        }
    }

    // --- Example usage ---
    public static void main(String[] args) throws Exception {
        Retryer retryer = Retryer.of(3, Duration.ofSeconds(1));

        String result = retryer.call(() -> {
            if (Math.random() < 0.7) {
                throw new RuntimeException("transient failure");
            }
            return "success";
        });
        System.out.println(result);

        retryer.run(() -> {
            System.out.println("side-effecting operation");
        });
    }
}
