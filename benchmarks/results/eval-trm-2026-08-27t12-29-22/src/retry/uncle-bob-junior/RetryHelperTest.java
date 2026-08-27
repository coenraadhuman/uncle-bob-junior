import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class RetryHelperTest {
    
    @Test
    void successOnFirstAttempt() throws Exception {
        RetryHelper retry = new RetryHelper(3, 10);
        
        int result = retry.execute(() -> 42);
        
        assertEquals(42, result);
    }
    
    @Test
    void successAfterRetry() throws Exception {
        RetryHelper retry = new RetryHelper(3, 10);
        AtomicInteger attempts = new AtomicInteger(0);
        
        int result = retry.execute(() -> {
            if (attempts.incrementAndGet() < 2) {
                throw new RuntimeException("Temporary failure");
            }
            return 99;
        });
        
        assertEquals(99, result);
        assertEquals(2, attempts.get());
    }
    
    @Test
    void failureAfterExhaustingRetries() {
        RetryHelper retry = new RetryHelper(2, 10);
        
        Exception thrown = assertThrows(RuntimeException.class, () -> {
            retry.execute(() -> {
                throw new RuntimeException("Persistent failure");
            });
        });
        
        assertEquals("Persistent failure", thrown.getMessage());
    }
    
    @Test
    void respectsDelayBetweenRetries() throws Exception {
        RetryHelper retry = new RetryHelper(2, 50);
        AtomicInteger attempts = new AtomicInteger(0);
        long start = System.currentTimeMillis();
        
        try {
            retry.execute(() -> {
                attempts.incrementAndGet();
                throw new RuntimeException("Always fails");
            });
        } catch (RuntimeException ignored) {
        }
        
        long elapsed = System.currentTimeMillis() - start;
        assertEquals(2, attempts.get());
        assertTrue(elapsed >= 50, "Should delay at least 50ms between retries");
    }
    
    @Test
    void rejectsInvalidMaxAttempts() {
        assertThrows(IllegalArgumentException.class, () -> new RetryHelper(0, 100));
        assertThrows(IllegalArgumentException.class, () -> new RetryHelper(-1, 100));
    }
    
    @Test
    void rejectsNegativeDelay() {
        assertThrows(IllegalArgumentException.class, () -> new RetryHelper(3, -1));
    }
}
