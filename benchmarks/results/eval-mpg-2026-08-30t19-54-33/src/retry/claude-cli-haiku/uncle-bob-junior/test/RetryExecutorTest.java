import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RetryExecutorTest {
    private static final int DELAY_MS = 10;
    private RetryExecutor<String> executor;
    
    @BeforeEach
    void setUp() {
        executor = new RetryExecutor<>(3, DELAY_MS);
    }
    
    @Test
    void succeedsOnFirstAttempt() throws Exception {
        String result = executor.execute(() -> "success");
        assertEquals("success", result);
    }
    
    @Test
    void retriesUntilSuccess() throws Exception {
        AtomicInteger attempts = new AtomicInteger(0);
        
        String result = executor.execute(() -> {
            if (attempts.incrementAndGet() < 3) {
                throw new RuntimeException("Fail attempt");
            }
            return "success";
        });
        
        assertEquals("success", result);
        assertEquals(3, attempts.get());
    }
    
    @Test
    void throwsRetryExhaustedAfterMaxAttempts() {
        assertThrows(RetryExhaustedException.class, () -> {
            executor.execute(() -> {
                throw new RuntimeException("Always fails");
            });
        });
    }
    
    @Test
    void retryExhaustedCauseIsLastException() {
        RuntimeException originalException = new RuntimeException("test error");
        
        RetryExhaustedException thrown = assertThrows(RetryExhaustedException.class, () -> {
            executor.execute(() -> {
                throw originalException;
            });
        });
        
        assertEquals(originalException, thrown.getCause());
    }
    
    @Test
    void supportsOperationsThatReturnNull() throws Exception {
        String result = executor.execute(() -> null);
        assertNull(result);
    }
    
    @Test
    void rejectsInvalidMaxAttempts() {
        assertThrows(IllegalArgumentException.class, () -> new RetryExecutor<>(0, 100));
    }
    
    @Test
    void rejectsNegativeDelay() {
        assertThrows(IllegalArgumentException.class, () -> new RetryExecutor<>(3, -1));
    }
}
