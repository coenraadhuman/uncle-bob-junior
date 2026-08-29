import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

public class RetryHelperTest {
    private static final int MAX_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 10;
    private RetryConfig config;

    @org.junit.Before
    public void setup() {
        config = new RetryConfig(MAX_ATTEMPTS, RETRY_DELAY_MS);
    }

    @org.junit.Test
    public void succeedsOnFirstAttempt() throws Exception {
        Callable<String> operation = () -> "success";
        String result = RetryHelper.executeWithRetry(operation, config);
        assert result.equals("success");
    }

    @org.junit.Test
    public void succeedsAfterRetries() throws Exception {
        AtomicInteger attempts = new AtomicInteger(0);
        Callable<String> operation = () -> {
            if (attempts.incrementAndGet() < 3) {
                throw new Exception("Temporary failure");
            }
            return "success";
        };

        String result = RetryHelper.executeWithRetry(operation, config);
        assert result.equals("success");
        assert attempts.get() == 3;
    }

    @org.junit.Test
    public void throwsWhenAttemptsExhausted() {
        Callable<String> operation = () -> {
            throw new Exception("Always fails");
        };

        try {
            RetryHelper.executeWithRetry(operation, config);
            assert false : "Should have thrown";
        } catch (RetryExhaustedException e) {
            assert e.getCause() != null;
        }
    }

    @org.junit.Test
    public void respectsRetryDelay() throws Exception {
        AtomicInteger attempts = new AtomicInteger(0);
        Callable<String> operation = () -> {
            if (attempts.incrementAndGet() < 2) {
                throw new Exception("First attempt fails");
            }
            return "success";
        };

        long startTime = System.currentTimeMillis();
        RetryHelper.executeWithRetry(operation, config);
        long elapsed = System.currentTimeMillis() - startTime;

        assert elapsed >= RETRY_DELAY_MS : "Should delay between attempts";
    }

    @org.junit.Test
    public void usesSimpleOverload() throws Exception {
        Callable<String> operation = () -> "ok";
        String result = RetryHelper.executeWithRetry(operation, 3, 10);
        assert result.equals("ok");
    }
}
