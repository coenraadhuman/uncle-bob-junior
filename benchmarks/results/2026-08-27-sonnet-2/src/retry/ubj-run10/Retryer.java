// Retryer.java
import java.time.Duration;
import java.util.concurrent.Callable;

/**
 * Runs an operation, retrying it with a fixed delay between attempts when it
 * throws an exception.
 */
public final class Retryer {

    private final Sleeper sleeper;

    public Retryer() {
        this(Sleeper.threadSleep());
    }

    public Retryer(Sleeper sleeper) {
        this.sleeper = sleeper;
    }

    /**
     * @throws RetryExhaustedException if every attempt allowed by {@code config} fails
     */
    public <T> T execute(Callable<T> operation, RetryConfig config) throws RetryExhaustedException {
        Exception lastFailure = null;

        for (int attempt = 1; attempt <= config.maxAttempts(); attempt++) {
            try {
                return operation.call();
            } catch (Exception e) {
                lastFailure = e;
            }
            if (attempt < config.maxAttempts()) {
                waitBeforeNextAttempt(config.delay(), attempt, lastFailure);
            }
        }

        throw new RetryExhaustedException(config.maxAttempts(), lastFailure);
    }

    private void waitBeforeNextAttempt(Duration delay, int attemptsMade, Exception lastFailure)
            throws RetryExhaustedException {
        try {
            sleeper.sleep(delay);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new RetryExhaustedException(attemptsMade, lastFailure);
        }
    }
}
