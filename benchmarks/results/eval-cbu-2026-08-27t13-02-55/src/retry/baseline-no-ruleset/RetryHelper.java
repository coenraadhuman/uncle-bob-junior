public class RetryHelper {
  private final int maxAttempts;
  private final long delayMillis;
  private final Class<? extends Exception> retryOn;

  private RetryHelper(int maxAttempts, long delayMillis, 
                      Class<? extends Exception> retryOn) {
    this.maxAttempts = maxAttempts;
    this.delayMillis = delayMillis;
    this.retryOn = retryOn;
  }

  public static Builder builder() {
    return new Builder();
  }

  public <T> T execute(Callable<T> operation) throws Exception {
    Exception lastException = null;

    for (int attempt = 1; attempt <= maxAttempts; attempt++) {
      try {
        return operation.call();
      } catch (Exception e) {
        if (!retryOn.isInstance(e)) {
          throw e;
        }
        lastException = e;
        if (attempt < maxAttempts) {
          Thread.sleep(delayMillis);
        }
      }
    }

    throw lastException;
  }

  public static class Builder {
    private int maxAttempts = 3;
    private long delayMillis = 1000;
    private Class<? extends Exception> retryOn = Exception.class;

    public Builder maxAttempts(int maxAttempts) {
      this.maxAttempts = maxAttempts;
      return this;
    }

    public Builder delayMillis(long delayMillis) {
      this.delayMillis = delayMillis;
      return this;
    }

    public Builder retryOn(Class<? extends Exception> exceptionType) {
      this.retryOn = exceptionType;
      return this;
    }

    public RetryHelper build() {
      return new RetryHelper(maxAttempts, delayMillis, retryOn);
    }
  }
}
