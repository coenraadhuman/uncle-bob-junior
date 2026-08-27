public class RetryHelper {
  private final int maxAttempts;
  private final long delayMillis;

  public RetryHelper(int maxAttempts, long delayMillis) {
    if (maxAttempts < 1) {
      throw new IllegalArgumentException("maxAttempts must be at least 1");
    }
    if (delayMillis < 0) {
      throw new IllegalArgumentException("delayMillis cannot be negative");
    }
    this.maxAttempts = maxAttempts;
    this.delayMillis = delayMillis;
  }

  public <T> T execute(Callable<T> operation) throws Exception {
    Exception lastException = null;

    for (int attempt = 1; attempt <= maxAttempts; attempt++) {
      try {
        return operation.call();
      } catch (Exception e) {
        lastException = e;
        if (attempt < maxAttempts) {
          Thread.sleep(delayMillis);
        }
      }
    }

    throw lastException;
  }
}
