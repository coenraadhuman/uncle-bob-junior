I'll provide a flexible retry helper with two key assumptions:

1. **maxAttempts includes the initial attempt** (not additional retries)
2. **Delay only waits between attempts**, not after the final one

Here's the complete implementation:

```java
public class RetryHelper {
    private final int maxAttempts;
    private final long delayMs;
    
    public RetryHelper(int maxAttempts, long delayMs) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        if (delayMs < 0) {
            throw new IllegalArgumentException("delayMs cannot be negative");
        }
        this.maxAttempts = maxAttempts;
        this.delayMs = delayMs;
    }
    
    public <T> T execute(Callable<T> operation) throws Exception {
        Exception lastException = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts) {
                    Thread.sleep(delayMs);
                }
            }
        }
        throw lastException;
    }
    
    public void execute(RunnableWithException operation) throws Exception {
        Exception lastException = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                operation.run();
                return;
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts) {
                    Thread.sleep(delayMs);
                }
            }
        }
        throw lastException;
    }
    
    @FunctionalInterface
    public interface RunnableWithException {
        void run() throws Exception;
    }
}
```

Here's how to use it:

```java
public class RetryExample {
    public static void main(String[] args) throws Exception {
        RetryHelper retrier = new RetryHelper(3, 1000); // 3 attempts, 1 second delay
        
        // Operation with return value
        String data = retrier.execute(() -> fetchFromAPI());
        System.out.println("Result: " + data);
        
        // Operation without return value
        retrier.execute(() -> writeToDatabase());
        System.out.println("Success!");
    }
    
    static String fetchFromAPI() throws Exception {
        if (Math.random() < 0.7) {
            throw new RuntimeException("API temporarily unavailable");
        }
        return "Data";
    }
    
    static void writeToDatabase() throws Exception {
        if (Math.random() < 0.6) {
            throw new Exception("Database connection failed");
        }
    }
}
```

The helper supports both operations returning values (via `Callable`) and operations without return values (via `RunnableWithException`). It handles both checked and unchecked exceptions.