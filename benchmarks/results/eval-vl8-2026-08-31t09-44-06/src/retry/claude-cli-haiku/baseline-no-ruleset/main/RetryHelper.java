public class RetryHelper {

  public interface RetryableOperation<T> {
    T execute() throws Exception;
  }

  public interface RetryableAction {
    void execute() throws Exception;
  }

  private static final int DEFAULT_MAX_ATTEMPTS = 3;
  private static final long DEFAULT_DELAY_MS = 1000;

  public static <T> T retry(RetryableOperation<T> operation, int maxAttempts, long delayMs)
      throws Exception {
    Exception lastException = null;

    for (int attempt = 1; attempt <= maxAttempts; attempt++) {
      try {
        return operation.execute();
      } catch (Exception e) {
        lastException = e;
        if (attempt < maxAttempts) {
          Thread.sleep(delayMs);
        }
      }
    }

    throw lastException;
  }

  public static void retry(RetryableAction action, int maxAttempts, long delayMs)
      throws Exception {
    retry(() -> {
      action.execute();
      return null;
    }, maxAttempts, delayMs);
  }

  public static <T> T retry(RetryableOperation<T> operation) throws Exception {
    return retry(operation, DEFAULT_MAX_ATTEMPTS, DEFAULT_DELAY_MS);
  }

  public static void retry(RetryableAction action) throws Exception {
    retry(action, DEFAULT_MAX_ATTEMPTS, DEFAULT_DELAY_MS);
  }
}
