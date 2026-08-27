// RetryHelper.java
package retry;

import java.time.Duration;

public final class RetryHelper {

    private final int maxAttempts;
    private final Duration delayBetweenAttempts;
    private final Sleeper sleeper;

    public RetryHelper(int maxAttempts, Duration delayBetweenAttempts) {
        this(maxAttempts, delayBetweenAttempts, new ThreadSleeper());
    }

    RetryHelper(int maxAttempts, Duration delayBetweenAttempts, Sleeper sleeper) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        if (delayBetweenAttempts.isNegative()) {
            throw new IllegalArgumentException("delayBetweenAttempts must not be negative");
        }
        this.maxAttempts = maxAttempts;
        this.delayBetweenAttempts = delayBetweenAttempts;
        this.sleeper = sleeper;
    }

    public void run(RetryableAction action) {
        run(() -> {
            action.run();
            return null;
        });
    }

    public <T> T run(RetryableOperation<T> operation) {
        Throwable lastFailure = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (Exception failure) {
                lastFailure = failure;
                if (attempt < maxAttempts) {
                    sleepBeforeRetry(attempt);
                }
            }
        }
        throw new RetryExhaustedException(maxAttempts, lastFailure);
    }

    private void sleepBeforeRetry(int attemptsCompleted) {
        try {
            sleeper.sleep(delayBetweenAttempts);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new RetryInterruptedException(attemptsCompleted, interrupted);
        }
    }
}
