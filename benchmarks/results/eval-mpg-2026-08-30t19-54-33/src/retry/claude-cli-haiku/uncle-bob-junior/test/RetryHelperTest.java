import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class RetryHelperTest {
    
    @Test
    void successOnFirstAttempt() throws Exception {
        RetryHelper retry = new RetryHelper(3, 0);
        int result = retry.execute(() -> 42);
        assertEquals(42, result);
    }
    
    @Test
    void succeedsAfterRetries() throws Exception {
        RetryHelper retry = new RetryHelper(3, 0);
        AtomicInteger attempts = new AtomicInteger(0);
        
        int result = retry.execute(() -> {
            attempts.incrementAndGet();
            if (attempts.get() < 3) throw new RuntimeException("Not ready");
            return 99;
        });
        
        assertEquals(3, attempts.get());
        assertEquals(99, result);
    }
    
    @Test
    void throwsExceptionAfterMaxAttemptsExceeded() {
        RetryHelper retry = new RetryHelper(2, 0);
        
        assertThrows(RuntimeException.class, () -> {
            retry.execute(() -> {
                throw new RuntimeException("Always fails");
            });
        });
    }
    
    @Test
    void waitsDelayMillisBetweenAttempts() throws Exception {
        RetryHelper retry = new RetryHelper(3, 50);
        AtomicInteger attempts = new AtomicInteger(0);
        long startTime = System.currentTimeMillis();
        
        try {
            retry.execute(() -> {
                attempts.incrementAndGet();
                throw new RuntimeException("Always fails");
            });
        } catch (RuntimeException ignored) {
        }
        
        long elapsed = System.currentTimeMillis() - startTime;
        assertTrue(elapsed >= 100, "Expected >= 100ms delay, got " + elapsed);
    }
    
    @Test
    void supportsVoidOperations() throws Exception {
        RetryHelper retry = new RetryHelper(3, 0);
        AtomicInteger counter = new AtomicInteger(0);
        
        retry.executeVoid(() -> counter.incrementAndGet());
        
        assertEquals(1, counter.get());
    }
    
    @Test
    void retriesVoidOperations() throws Exception {
        RetryHelper retry = new RetryHelper(3, 0);
        AtomicInteger attempts = new AtomicInteger(0);
        
        retry.executeVoid(() -> {
            attempts.incrementAndGet();
            if (attempts.get() < 2) throw new RuntimeException("Not ready");
        });
        
        assertEquals(2, attempts.get());
    }
    
    @Test
    void rejectsZeroMaxAttempts() {
        assertThrows(IllegalArgumentException.class, () -> new RetryHelper(0, 100));
    }
    
    @Test
    void rejectsNegativeMaxAttempts() {
        assertThrows(IllegalArgumentException.class, () -> new RetryHelper(-1, 100));
    }
    
    @Test
    void rejectsNegativeDelay() {
        assertThrows(IllegalArgumentException.class, () -> new RetryHelper(3, -1));
    }
    
    @Test
    void usesDefaultConstructor() throws Exception {
        RetryHelper retry = new RetryHelper();
        int result = retry.execute(() -> 42);
        assertEquals(42, result);
    }
}
