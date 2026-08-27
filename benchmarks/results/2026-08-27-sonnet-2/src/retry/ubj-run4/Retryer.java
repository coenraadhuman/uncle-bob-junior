import java.util.concurrent.Callable;

public final class Retryer {

    private final RetryConfig config;
    private final Sleeper sleeper;

    public Retryer(RetryConfig config) {
        this(config, duration -> Thread.sleep(duration.toMillis()));
    }

    // package-private: lets tests inject a no-op/recording Sleeper instead of really waiting
    Retryer(RetryConfig config, Sleeper sleeper) {
        this.config = config;
        this.sleeper = sleeper;
    }

    public <T> T execute(Callable<T> operation) {
        Exception lastFailure = null;
        for (int attempt = 1; attempt <= config.maxAttempts(); attempt++) {
            try {
                return operation.call();
            } catch (Exception e) {
                lastFailure = e;
                waitBeforeNextAttempt(attempt);
            }
        }
        throw new RetryExhaustedException(config.maxAttempts(), lastFailure);
    }

    private void waitBeforeNextAttempt(int attempt) {
        boolean hasMoreAttempts = attempt < config.maxAttempts();
        if (!hasMoreAttempts) {
            return;
        }
        sleep();
    }

    private void sleep() {
        try {
            sleeper.sleep(config.delay());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Retry interrupted while waiting", e);
        }
    }
}
