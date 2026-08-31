import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class RetryHelperTest {
    
    @Test
    void succeedsOnFirstAttempt() throws Exception {
        RetryHelper retry = new RetryHelper(3, 10);
        String result = retry.executeWithRetry(() -> "success");
        assertEquals("success", result);
    }
    
    @Test
    void succeedsAfterRetries() throws Exception {
        RetryHelper retry = new RetryHelper(3, 10);
        AtomicInteger attempts = new AtomicInteger(0);
        
        String result = retry.executeWithRetry(() -> {
            attempts.incrementAndGet();
            if (attempts.get() < 3) {
                throw new RuntimeException("Fail");
            }
            return "success";
        });
        
        assertEquals("success", result);
        assertEquals(3, attempts.get());
    }
    
    @Test
    void throwsExceptionAfterMaxAttempts() {
        RetryHelper retry = new RetryHelper(2, 10);
        
        assertThrows(RuntimeException.class, () -> {
            retry.executeWithRetry(() -> {
                throw new RuntimeException("Always fails");
            });
        });
    }
    
    @Test
    void respectsDelayBetweenAttempts() throws Exception {
        RetryHelper retry = new RetryHelper(3, 50);
        AtomicInteger attempts = new AtomicInteger(0);
        long start = System.currentTimeMillis();
        
        retry.executeWithRetry(() -> {
            attempts.incrementAndGet();
            if (attempts.get() < 3) {
                throw new RuntimeException("Fail");
            }
            return "success";
        });
        
        long elapsed = System.currentTimeMillis() - start;
        assertTrue(elapsed >= 100, "Should wait at least 100ms for 2 delays of 50ms each");
    }
    
    @Test
    void usesDefaultValues() throws Exception {
        RetryHelper retry = new RetryHelper();
        String result = retry.executeWithRetry(() -> "success");
        assertEquals("success", result);
    }
    
    @Test
    void rejectsZeroMaxAttempts() {
        assertThrows(IllegalArgumentException.class, () -> new RetryHelper(0, 10));
    }
    
    @Test
    void rejectsNegativeDelay() {
        assertThrows(IllegalArgumentException.class, () -> new RetryHelper(3, -1));
    }
}
