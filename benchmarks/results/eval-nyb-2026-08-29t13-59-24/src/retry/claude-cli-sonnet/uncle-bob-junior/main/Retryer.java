// Retryer.java
package retry;

import java.time.Duration;
import java.util.concurrent.Callable;

/** Runs an operation, retrying it on failure according to a {@link RetryPolicy}. */
public final class Retryer {

    private final Sleeper sleeper;

    public Retryer() {
        this(duration -> Thread.sleep(duration.toMillis()));
    }

    Retryer(Sleeper sleeper) {
        this.sleeper = sleeper;
    }

    /**
     * @throws RetryExhaustedException if every attempt fails
     * @throws RetryInterruptedException if interrupted while waiting between attempts
     */
    public <T> T execute(Callable<T> operation, RetryPolicy policy) {
        Exception lastFailure = null;
        for (int attempt = 1; attempt <= policy.maxAttempts(); attempt++) {
            try {
                return operation.call();
            } catch (Exception failure) {
                lastFailure = failure;
                sleepUnlessFinalAttempt(attempt, policy);
            }
        }
        throw new RetryExhaustedException(policy.maxAttempts(), lastFailure);
    }

    private void sleepUnlessFinalAttempt(int attempt, RetryPolicy policy) {
        if (attempt == policy.maxAttempts()) {
            return;
        }
        waitBeforeRetry(policy.delayBetweenAttempts());
    }

    private void waitBeforeRetry(Duration delay) {
        try {
            sleeper.sleep(delay);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new RetryInterruptedException(interrupted);
        }
    }
}
