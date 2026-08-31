```java
import java.util.concurrent.Callable;

public class RetryHelper {
    @FunctionalInterface
    public interface CheckedRunnable {
        void run() throws Exception;
    }

    @FunctionalInterface
    public interface CheckedSupplier<T> {
        T get() throws Exception;
    }

    public static <T> T retry(CheckedSupplier<T> operation, int maxAttempts, long delayMillis) throws Exception {
        Exception lastException = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.get();
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts) {
                    Thread.sleep(delayMillis);
                }
            }
        }
        throw lastException;
    }

    public static void retry(CheckedRunnable operation, int maxAttempts, long delayMillis) throws Exception {
        Exception lastException = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                operation.run();
                return;
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts) {
                    Thread.sleep(delayMillis);
                }
            }
        }
        if (lastException != null) {
            throw lastException;
        }
    }
}
```

**Example usage:**

```java
// Operation that returns a value
String result = RetryHelper.retry(
    () -> fetchData(), 
    3,           // max attempts
    1000         // delay in milliseconds
);

// Operation without return value
RetryHelper.retry(
    () -> sendRequest(),
    5,
    2000
);
```

**Key features:**
- Generic type support for operations that return values
- Custom functional interfaces that allow checked exceptions
- Waits `delayMillis` between retry attempts
- Throws the last caught exception after exhausting retries
- Minimal and focused—no logging or extra features beyond the core requirement