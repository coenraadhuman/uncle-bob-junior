package retry;

import java.time.Duration;

public final class Retryer {

    private final Sleeper sleeper;

    public Retryer() {
        this(delay -> Thread.sleep(delay.toMillis()));
    }

    Retryer(Sleeper sleeper) {
        this.sleeper = sleeper;
    }

    /**
     * Runs {@code operation}, retrying on any {@link Exception} up to
     * {@code policy.maxAttempts()} times, waiting {@code policy.delayBetweenAttempts()}
     * between attempts (never after the final one).
     *
     * @throws RetryExhaustedException if every attempt failed; its cause is the last failure.
     */
    public <T> T run(RetryableOperation<T> operation, RetryPolicy policy) {
        Exception lastFailure = null;

        for (int attempt = 1; attempt <= policy.maxAttempts(); attempt++) {
            try {
                return operation.execute();
            } catch (Exception failure) {
                lastFailure = failure;
            }
            if (attempt == policy.maxAttempts()) {
                break;
            }
            sleepBetweenAttempts(policy.delayBetweenAttempts());
        }

        throw new RetryExhaustedException(policy.maxAttempts(), lastFailure);
    }

    private void sleepBetweenAttempts(Duration delay) {
        try {
            sleeper.sleep(delay);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            throw new RetryInterruptedException(interruptedException);
        }
    }
}
