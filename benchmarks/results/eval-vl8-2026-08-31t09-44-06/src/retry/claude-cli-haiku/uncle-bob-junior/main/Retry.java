import java.util.concurrent.Callable;

public class Retry {
  private static final int DEFAULT_MAX_ATTEMPTS = 3;
  private static final long DEFAULT_DELAY_MS = 1000;

  private final int maxAttempts;
  private final long delayMs;

  private Retry(int maxAttempts, long delayMs) {
    validateMaxAttempts(maxAttempts);
    this.maxAttempts = maxAttempts;
    this.delayMs = delayMs;
  }

  public <R> R execute(Callable<R> operation) throws Exception {
    Exception lastException = null;
    
    for (int attempt = 1; attempt <= maxAttempts; attempt++) {
      try {
        return operation.call();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw e;
      } catch (Exception e) {
        lastException = e;
        delayBeforeNextAttempt(attempt);
      }
    }
    
    throw lastException;
  }

  private void delayBeforeNextAttempt(int attempt) throws InterruptedException {
    if (attempt < maxAttempts) {
      Thread.sleep(delayMs);
    }
  }

  private static void validateMaxAttempts(int maxAttempts) {
    if (maxAttempts < 1) {
      throw new IllegalArgumentException("maxAttempts must be at least 1");
    }
  }

  public static RetryBuilder builder() {
    return new RetryBuilder();
  }

  public static class RetryBuilder {
    private int maxAttempts = DEFAULT_MAX_ATTEMPTS;
    private long delayMs = DEFAULT_DELAY_MS;

    public RetryBuilder maxAttempts(int attempts) {
      this.maxAttempts = attempts;
      return this;
    }

    public RetryBuilder delayMs(long delay) {
      this.delayMs = delay;
      return this;
    }

    public <R> R execute(Callable<R> operation) throws Exception {
      return new Retry(maxAttempts, delayMs).execute(operation);
    }
  }
}
